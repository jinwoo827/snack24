#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
# snack24 End-to-End Demo
#
# 흐름:
#   1. 기존 k3d 클러스터 삭제 + 새 클러스터 생성
#   2. NGINX Ingress 설치
#   3. Gradle 빌드 → Docker 이미지 → k3d import
#   4. dev overlay 배포
#   5. 회사 등록 → 로그인 → 상품 등록(다건) → 주문 시도(실패)
#      → 지갑 충전 → 주문 성공 → 결과 확인
#
# 실행: bash scripts/demo.sh [phase]
#   phase 를 지정하면 그 단계부터만 실행 (예: bash scripts/demo.sh app)
#     all         (기본) — 전체 실행 (monitoring 제외)
#     infra       — 클러스터 초기화 + Ingress
#     app         — 빌드 + import + 배포
#     scenario    — 비즈니스 시나리오만
#     monitoring  — Prometheus + Grafana 설치 (kube-prometheus-stack)
#
# 요구사항: docker, k3d, kubectl, helm, jq, curl
# ─────────────────────────────────────────────────────────────

set -euo pipefail

# ═════════════════════════════════════════════════════════════
# 설정
# ═════════════════════════════════════════════════════════════

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 모드 : local | ghcr
IMAGE_MODE="${IMAGE_MODE:-local}"

GHCR_OWNER="jinwoo827"
GHCR_TAG="main"
LOCAL_TAG="latest"
CLUSTER_NAME="snack24"
NAMESPACE="snack24"
INGRESS_HOST="dev.snack24.local"
GATEWAY_URL="http://${INGRESS_HOST}:8080"

SERVICES=(identity catalog order billing api-gateway notification-worker)

# 시나리오 데이터
COMPANY_NAME="데모 컴퍼니"
BUSINESS_NO="123-45-67890"
ADMIN_EMAIL="admin@demo.com"
ADMIN_PASSWORD="P@ssw0rd!"
ADMIN_NAME="관리자"

# name:price:stock
PRODUCTS=(
  "초코바:1500"
  "감자칩:2000"
  "쿠키:1000"
  "탄산음료:1800"
  "젤리:1200"
)

WALLET_TOPUP_AMOUNT=100000

# 재고 입고 수량 (테스트 시나리오용)
STOCK_INBOUND_MAIN=100          # 첫 번째 상품 — 충분한 재고
STOCK_INBOUND_LIMITED=5         # 두 번째 상품 — 제한된 재고 (초과 주문 시연용)

# 주문 수량
ORDER_MAIN_QTY=2                # 정상 주문에서 첫 상품
ORDER_LIMITED_QTY=1             # 정상 주문에서 둘째 상품
ORDER_EXCEED_QTY=10             # 초과 주문 수량 (재고 부족 시연)

# ═════════════════════════════════════════════════════════════
# 유틸리티
# ═════════════════════════════════════════════════════════════

RED='\033[0;31m'
GRN='\033[0;32m'
YLW='\033[1;33m'
BLU='\033[0;34m'
CYN='\033[0;36m'
NC='\033[0m'

log()  { echo -e "${BLU}[$(date +%H:%M:%S)]${NC} $*"; }
step() { echo -e "\n${YLW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${YLW}▶ $*${NC}"
        echo -e "${YLW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"; }
sub()  { echo -e "\n${CYN}◆ $*${NC}"; }
ok()   { echo -e "${GRN}✓${NC} $*"; }
warn() { echo -e "${YLW}⚠${NC} $*"; }
err()  { echo -e "${RED}✗${NC} $*" >&2; exit 1; }

require() {
  for cmd in "$@"; do
    command -v "$cmd" >/dev/null 2>&1 || err "필수 도구 없음: $cmd"
  done
}

wait_http_200() {
  local url="$1"; local timeout="${2:-120}"
  local elapsed=0
  while (( elapsed < timeout )); do
    if curl -sf -o /dev/null "$url"; then return 0; fi
    sleep 2; elapsed=$((elapsed + 2))
  done
  return 1
}

pretty_json() {
  if command -v jq >/dev/null; then jq -C .; else cat; fi
}

http_call() {
  local method="$1"; shift
  local url="$1"; shift
  local body="${1-}"; [[ $# -gt 0 ]] && shift

  local args=(-s -w "\n__HTTP_STATUS__%{http_code}")
  args+=(-X "$method" "$url")
  [[ -n "$body" ]] && args+=(-d "$body")

  local response; response=$(curl "${args[@]}" "$@")
  local status_line; status_line=$(echo "$response" | grep "__HTTP_STATUS__" | tail -1)
  local status="${status_line##*__HTTP_STATUS__}"
  local content; content=$(echo "$response" | sed '/__HTTP_STATUS__/d')

  echo "$status"
  echo "$content"
}

# ═════════════════════════════════════════════════════════════
# Phase 1 — 인프라 초기화
# ═════════════════════════════════════════════════════════════
phase_infra() {
  step "Phase 1  인프라 초기화"

  sub "필수 도구 체크"
  require docker k3d kubectl helm jq curl
  ok "도구 준비됨"

  sub "기존 k3d 클러스터 정리"
  if k3d cluster list 2>/dev/null | grep -q "^${CLUSTER_NAME}\b"; then
    log "'${CLUSTER_NAME}' 클러스터 삭제"
    k3d cluster delete "$CLUSTER_NAME"
    ok "삭제 완료"
  else
    log "삭제할 클러스터 없음"
  fi

  sub "k3d 클러스터 생성"
  k3d cluster create "$CLUSTER_NAME" \
    --agents 0 \
    --port "8080:80@loadbalancer" \
    --port "8443:443@loadbalancer" \
    --k3s-arg "--disable=traefik@server:0"

  log "노드 Ready 대기"
  kubectl wait --for=condition=Ready node --all --timeout=120s
  ok "클러스터 준비됨"

  sub "NGINX Ingress 설치"
  helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx 2>/dev/null || true
  helm repo update >/dev/null

  helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
    -n ingress-nginx --create-namespace \
    --set controller.publishService.enabled=true \
    --wait --timeout 3m
  ok "NGINX Ingress 준비됨"

  sub "/etc/hosts 확인"
  if grep -q "$INGRESS_HOST" /etc/hosts; then
    ok "${INGRESS_HOST} 등록되어 있음"
  else
    warn "${INGRESS_HOST} 가 /etc/hosts 에 없음"
    echo -e "  다음을 별도 실행하세요 (sudo 필요):"
    echo -e "  ${CYN}sudo sh -c 'echo \"127.0.0.1 ${INGRESS_HOST}\" >> /etc/hosts'${NC}"
    read -r -p "  등록 후 Enter 를 눌러 계속..." _
  fi
}

# ═════════════════════════════════════════════════════════════
# Phase 2 — 애플리케이션 빌드 + 배포
# ═════════════════════════════════════════════════════════════
phase_app() {
  step "Phase 2  애플리케이션 준비 (mode=${IMAGE_MODE})"

  # 최종 이미지 참조를 결정 (모드별)
  local target_image_prefix target_image_tag
  case "$IMAGE_MODE" in
    local)
      target_image_prefix="snack24/"
      target_image_tag="${LOCAL_TAG}"

      sub "Gradle bootJar"
      ./gradlew bootJar --no-daemon --parallel

      sub "Docker 이미지 빌드"
      for svc in "${SERVICES[@]}"; do
        log "빌드: snack24/${svc}:${LOCAL_TAG}"
        docker build -q -t "snack24/${svc}:${LOCAL_TAG}" "service/${svc}" > /dev/null
      done

      sub "k3d 이미지 import"
      local images=()
      for svc in "${SERVICES[@]}"; do
        images+=("snack24/${svc}:${LOCAL_TAG}")
      done
      k3d image import "${images[@]}" -c "$CLUSTER_NAME"
      ;;

    ghcr)
      target_image_prefix="ghcr.io/${GHCR_OWNER}/snack24/"
      target_image_tag="${GHCR_TAG}"

      sub "GHCR 이미지 pull"
      for svc in "${SERVICES[@]}"; do
        log "pull: ghcr.io/${GHCR_OWNER}/snack24/${svc}:${GHCR_TAG}"
        docker pull "ghcr.io/${GHCR_OWNER}/snack24/${svc}:${GHCR_TAG}"
      done

      sub "k3d 이미지 import"
      local images=()
      for svc in "${SERVICES[@]}"; do
        images+=("ghcr.io/${GHCR_OWNER}/snack24/${svc}:${GHCR_TAG}")
      done
      k3d image import "${images[@]}" -c "$CLUSTER_NAME"
      ;;

    *) err "IMAGE_MODE 는 local 또는 ghcr" ;;
  esac

  sub "매니페스트 렌더링 + 이미지 참조 치환 + 배포"
  kubectl kustomize infra/k8s/overlays/dev \
    | sed -E "s|image: (ghcr\\.io/[^/]+/)?snack24/([a-zA-Z0-9-]+):[^[:space:]]+|image: ${target_image_prefix}\\2:${target_image_tag}|g" \
    | kubectl apply -f -

  ok "배포 요청 완료"

  sub "Pod Ready 대기 (최대 5분)"
  kubectl wait -n "$NAMESPACE" \
    --for=condition=Ready pod \
    -l app.kubernetes.io/part-of=snack24 \
    --timeout=300s
  ok "모든 Pod Ready"

  sub "Gateway 헬스체크"
  if wait_http_200 "${GATEWAY_URL}/actuator/health" 120; then
    ok "Gateway 응답 정상"
  else
    err "Gateway 응답 없음 — Pod 상태 확인 필요: kubectl get pods -n ${NAMESPACE}"
  fi

  kubectl get pods -n "$NAMESPACE"
}

# ═════════════════════════════════════════════════════════════
# Phase 3 — 비즈니스 시나리오
# ═════════════════════════════════════════════════════════════
phase_scenario() {
  step "Phase 3  비즈니스 시나리오"

  # ─────────────────────────────────────────────
  # 3-1. 회사 등록 (관리자 계정 포함)
  # ─────────────────────────────────────────────
  sub "3-1. 회사 등록"
  local company_body
  company_body=$(cat <<EOF
{
  "name": "${COMPANY_NAME}",
  "businessNo": "${BUSINESS_NO}",
  "admin": {
    "email": "${ADMIN_EMAIL}",
    "password": "${ADMIN_PASSWORD}",
    "name": "${ADMIN_NAME}",
    "phone": "01000000000"
  }
}
EOF
)
  local result status body
  result=$(http_call POST "${GATEWAY_URL}/v1/companies" "$company_body" \
    -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)

  echo "$body" | pretty_json
  case "$status" in
    2*) ok "회사 등록 성공 (HTTP ${status})" ;;
    409|400)
      warn "회사가 이미 존재하는 것 같음 (HTTP ${status}). 진행 계속." ;;
    *) err "회사 등록 실패 (HTTP ${status})" ;;
  esac

  # ─────────────────────────────────────────────
  # 3-2. 로그인 → 토큰 획득
  # ─────────────────────────────────────────────
  sub "3-2. 관리자 로그인 → JWT 획득"
  local login_body
  login_body=$(cat <<EOF
{
  "email": "${ADMIN_EMAIL}",
  "password": "${ADMIN_PASSWORD}"
}
EOF
)
  result=$(http_call POST "${GATEWAY_URL}/v1/auth/login" "$login_body" \
    -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)

  [[ "$status" =~ ^2 ]] || { echo "$body" | pretty_json; err "로그인 실패 (HTTP ${status})"; }

  local token
  token=$(echo "$body" | jq -r '.accessToken // .access_token // empty')
  [[ -n "$token" ]] || { echo "$body" | pretty_json; err "accessToken 파싱 실패"; }
  ok "토큰 획득 (앞 20자: ${token:0:20}...)"

  local auth_header="Authorization: Bearer ${token}"

  # ─────────────────────────────────────────────
  # 3-3. 상품 등록 (5개)
  # ─────────────────────────────────────────────
  sub "3-3. 상품 5개 등록"
  local -a product_ids=()
  for p in "${PRODUCTS[@]}"; do
    IFS=':' read -r name price stock <<< "$p"
    local prod_body
    prod_body=$(cat <<EOF
{
  "name": "${name}",
  "unitPrice": ${price},
  "description": "${name} 상세설명",
  "category": "BEVERAGE"
}
EOF
)
    result=$(http_call POST "${GATEWAY_URL}/v1/products" "$prod_body" \
      -H "$auth_header" -H "Content-Type: application/json")
    status=$(echo "$result" | head -1)
    body=$(echo "$result" | tail -n +2)

    if [[ "$status" =~ ^2 ]]; then
      local pid; pid=$(echo "$body" | jq -r '.id // .productId // empty')
      product_ids+=("$pid")
      log "✓ ${name} (id=${pid}, price=${price}})"
    else
      warn "${name} 등록 실패 (HTTP ${status}): $body"
    fi
  done
  ok "총 ${#product_ids[@]} 개 상품 등록"

  # ─────────────────────────────────────────────
  # 3-4. 상품 목록 조회
  # ─────────────────────────────────────────────
  sub "3-4. 상품 목록 조회"
  result=$(http_call GET "${GATEWAY_URL}/v1/products" "" -H "$auth_header")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)
  echo "$body" | pretty_json
  [[ "$status" =~ ^2 ]] && ok "조회 성공" || err "조회 실패 (HTTP ${status})"

  # 실제 첫 두 상품 id 를 응답에서 다시 추출 (안전)
  local pid1 pid2
  pid1=$(echo "$body" | jq -r '(.content // .)[0].id // (.content // .)[0].productId')
  pid2=$(echo "$body" | jq -r '(.content // .)[1].id // (.content // .)[1].productId')
  [[ "$pid1" != "null" && -n "$pid1" ]] || err "상품 id 파싱 실패"

  # ─────────────────────────────────────────────
  # 정상 주문 페이로드 (Scenario A/B/C 공용)
  # ─────────────────────────────────────────────
  local order_body
  order_body=$(cat <<EOF
{
  "items": [
    {"productId": ${pid1}, "quantity": ${ORDER_MAIN_QTY}},
    {"productId": ${pid2}, "quantity": ${ORDER_LIMITED_QTY}}
  ]
}
EOF
)

  # ─────────────────────────────────────────────
  # [Scenario A] 잔액 없이 + 재고 없이 주문 → 실패
  # ─────────────────────────────────────────────
  sub "3-5. [Scenario A] 잔액 없이 + 재고 없이 주문 시도"
  log "예상 결과: 실패 (Saga 실패 — 잔액 또는 재고 부족)"
  result=$(http_call POST "${GATEWAY_URL}/v1/orders" "$order_body" \
    -H "$auth_header" -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)
  echo "$body" | pretty_json

  local order_a_id
  order_a_id=$(echo "$body" | jq -r '.orderId // .id // empty')

  log "Saga 완료 대기 (5초)..."
  sleep 5

  if [[ -n "$order_a_id" && "$order_a_id" != "null" ]]; then
    log "주문 상태 조회 → CANCELED 여야 정상"
    result=$(http_call GET "${GATEWAY_URL}/v1/orders/${order_a_id}" "" -H "$auth_header")
    echo "$result" | tail -n +2 | pretty_json
  else
    log "주문 생성 즉시 거부됨 (Saga 시작 전 사전 검증 실패)"
  fi
  ok "[Scenario A] 실패 확인"

  # ─────────────────────────────────────────────
  # [Scenario B] 잔액만 충전 후 주문 → 재고 부족 실패
  # ─────────────────────────────────────────────
  sub "3-6. 지갑 충전 (${WALLET_TOPUP_AMOUNT}원)"
  local topup_body
  topup_body=$(cat <<EOF
{
  "amount": ${WALLET_TOPUP_AMOUNT}
}
EOF
)
  result=$(http_call POST "${GATEWAY_URL}/v1/wallets/charge" "$topup_body" \
    -H "$auth_header" -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)
  echo "$body" | pretty_json
  [[ "$status" =~ ^2 ]] && ok "충전 성공" || warn "충전 실패 (HTTP ${status}) — API 경로 확인 필요"

  log "잔액 재조회"
  result=$(http_call GET "${GATEWAY_URL}/v1/wallets" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json

  sub "3-7. [Scenario B] 잔액 있음 + 재고 없음 상태에서 주문 시도"
  log "예상 결과: 실패 (재고 부족 — StockReservationFailed)"
  result=$(http_call POST "${GATEWAY_URL}/v1/orders" "$order_body" \
    -H "$auth_header" -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)
  echo "$body" | pretty_json

  local order_b_id
  order_b_id=$(echo "$body" | jq -r '.orderId // .id // empty')
  sleep 5

  if [[ -n "$order_b_id" && "$order_b_id" != "null" ]]; then
    log "주문 상태 조회 → CANCELED 여야 정상"
    result=$(http_call GET "${GATEWAY_URL}/v1/orders/${order_b_id}" "" -H "$auth_header")
    echo "$result" | tail -n +2 | pretty_json
  fi

  log "잔액 확인 (Saga 실패 시 롤백되어 그대로 유지되어야 함)"
  result=$(http_call GET "${GATEWAY_URL}/v1/wallets" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json
  ok "[Scenario B] 실패 확인 (지갑 잔액 유지 확인)"

  # ─────────────────────────────────────────────
  # 재고 입고
  # ─────────────────────────────────────────────
  sub "3-8. 재고 입고"
  log "첫 상품 (pid=${pid1}) : ${STOCK_INBOUND_MAIN} 개 입고"
  log "둘째 상품 (pid=${pid2}) : ${STOCK_INBOUND_LIMITED} 개 입고 (제한된 재고)"

  inbound_stock() {
    local pid="$1" qty="$2"
    local inbound_body
    inbound_body=$(cat <<EOF
{
  "quantity": ${qty}
}
EOF
)
    result=$(http_call POST "${GATEWAY_URL}/v1/products/${pid}/stock/replenish" "$inbound_body" \
      -H "$auth_header" -H "Content-Type: application/json")
    status=$(echo "$result" | head -1)
    body=$(echo "$result" | tail -n +2)
    echo "$body" | pretty_json
    [[ "$status" =~ ^2 ]] && ok "입고 성공 (pid=${pid}, qty=${qty})" \
      || warn "입고 실패 (HTTP ${status}) — 입고 API 경로 확인 필요"
  }

  inbound_stock "$pid1" "$STOCK_INBOUND_MAIN"
  inbound_stock "$pid2" "$STOCK_INBOUND_LIMITED"

  log "재고 확인"
  result=$(http_call GET "${GATEWAY_URL}/v1/products/${pid1}" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json
  result=$(http_call GET "${GATEWAY_URL}/v1/products/${pid2}" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json

  # ─────────────────────────────────────────────
  # [Scenario C] 잔액 + 재고 모두 있음 → 주문 성공
  # ─────────────────────────────────────────────
  sub "3-9. [Scenario C] 잔액 있음 + 재고 있음 상태에서 주문"
  log "예상 결과: 성공 (Saga 완료 — Order CONFIRMED)"
  result=$(http_call POST "${GATEWAY_URL}/v1/orders" "$order_body" \
    -H "$auth_header" -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)
  echo "$body" | pretty_json

  local order_c_id
  order_c_id=$(echo "$body" | jq -r '.orderId // .id // empty')
  [[ -n "$order_c_id" && "$order_c_id" != "null" ]] || err "orderId 파싱 실패"
  ok "주문 접수됨 (orderId=${order_c_id})"

  log "Saga 완료 대기 (7초)..."
  sleep 7

  log "주문 상태 → CONFIRMED 여야 정상"
  result=$(http_call GET "${GATEWAY_URL}/v1/orders/${order_c_id}" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json

  log "잔액 (차감 반영)"
  result=$(http_call GET "${GATEWAY_URL}/v1/wallets" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json

  log "재고 (차감 반영: pid=${pid1} → -${ORDER_MAIN_QTY}, pid=${pid2} → -${ORDER_LIMITED_QTY})"
  result=$(http_call GET "${GATEWAY_URL}/v1/products/${pid1}" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json
  result=$(http_call GET "${GATEWAY_URL}/v1/products/${pid2}" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json
  ok "[Scenario C] 성공 확인"

  # ─────────────────────────────────────────────
  # [Scenario D] 재고 초과 주문 → 실패
  # ─────────────────────────────────────────────
  sub "3-10. [Scenario D] 재고 초과 주문 시도"
  local remaining_pid2=$((STOCK_INBOUND_LIMITED - ORDER_LIMITED_QTY))
  log "pid=${pid2} 남은 재고: ${remaining_pid2} 개"
  log "요청 수량: ${ORDER_EXCEED_QTY} 개 (남은 재고 초과)"
  log "예상 결과: 실패 (StockReservationFailed)"

  local exceed_order_body
  exceed_order_body=$(cat <<EOF
{
  "items": [
    {"productId": ${pid2}, "quantity": ${ORDER_EXCEED_QTY}}
  ]
}
EOF
)
  result=$(http_call POST "${GATEWAY_URL}/v1/orders" "$exceed_order_body" \
    -H "$auth_header" -H "Content-Type: application/json")
  status=$(echo "$result" | head -1)
  body=$(echo "$result" | tail -n +2)
  echo "$body" | pretty_json

  local order_d_id
  order_d_id=$(echo "$body" | jq -r '.orderId // .id // empty')
  sleep 5

  if [[ -n "$order_d_id" && "$order_d_id" != "null" ]]; then
    log "주문 상태 조회 → CANCELED 여야 정상"
    result=$(http_call GET "${GATEWAY_URL}/v1/orders/${order_d_id}" "" -H "$auth_header")
    echo "$result" | tail -n +2 | pretty_json
  fi

  log "잔액 확인 (Saga 실패 시 롤백 → 3-9 후 상태 그대로)"
  result=$(http_call GET "${GATEWAY_URL}/v1/wallets" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json

  log "재고 확인 (Saga 실패 시 롤백 → 3-9 후 상태 그대로)"
  result=$(http_call GET "${GATEWAY_URL}/v1/products/${pid2}" "" -H "$auth_header")
  echo "$result" | tail -n +2 | pretty_json
  ok "[Scenario D] 실패 확인 (잔액·재고 롤백 여부 검증)"

}

# ═════════════════════════════════════════════════════════════
# Phase 4 — 관측성 스택 (Prometheus + Grafana)
# 별도 실행: bash scripts/demo.sh monitoring
# ═════════════════════════════════════════════════════════════
phase_monitoring() {
  step "Phase 4  관측성 스택 (Prometheus + Grafana)"

  sub "Helm repo 등록"
  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts 2>/dev/null || true
  helm repo update >/dev/null
  ok "repo 준비됨"

  sub "learn-env 용 values 생성"
  local values_file
  values_file="$(mktemp -t snack24-monitoring-values.XXXXXX.yaml)"
  cat > "$values_file" <<'EOF'
prometheus:
  prometheusSpec:
    retention: 3d
    resources:
      requests: { cpu: 200m, memory: 512Mi }
      limits:   { cpu: 1000m, memory: 1Gi }
    serviceMonitorSelectorNilUsesHelmValues: false
    podMonitorSelectorNilUsesHelmValues: false
grafana:
  adminPassword: "admin"
  resources:
    requests: { cpu: 100m, memory: 256Mi }
    limits:   { cpu: 500m, memory: 512Mi }
  persistence:
    enabled: false
alertmanager:
  alertmanagerSpec:
    resources:
      requests: { cpu: 50m, memory: 128Mi }
      limits:   { cpu: 200m, memory: 256Mi }
kubeControllerManager: { enabled: false }
kubeScheduler:         { enabled: false }
kubeProxy:             { enabled: false }
kubeEtcd:              { enabled: false }
EOF
  ok "values: $values_file"

  sub "kube-prometheus-stack 설치 (최대 5분)"
  helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
    -n monitoring --create-namespace \
    -f "$values_file" \
    --wait --timeout 5m
  ok "설치 완료"

  sub "monitoring 네임스페이스 Pod 상태"
  kubectl get pods -n monitoring

  sub "snack24 ServiceMonitor 등록 (Prometheus 가 앱 스크레이핑하도록)"
  if [[ -f infra/k8s/monitoring/service-monitors.yaml ]]; then
    kubectl apply -f infra/k8s/monitoring/service-monitors.yaml
    ok "ServiceMonitor 등록됨"
  else
    warn "infra/k8s/monitoring/service-monitors.yaml 없음 — 수동 등록 필요"
  fi

  # Grafana Ingress (있으면)
  if [[ -f infra/k8s/monitoring/grafana-ingress.yaml ]]; then
    kubectl apply -f infra/k8s/monitoring/grafana-ingress.yaml
    log "Grafana Ingress 등록됨 (있는 경우)"
  fi

  cat <<EOF

${GRN}✓ 관측성 스택 설치 완료${NC}

  ${CYN}접속 명령:${NC}
    kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
    → http://localhost:3000   (admin/admin)

    kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
    → http://localhost:9090

  ${CYN}대시보드
    17175 — Spring Boot 3.x Statistics
    4701  — JVM (Micrometer)
    15760 — Kubernetes / Views / Pods

EOF
}

# ═════════════════════════════════════════════════════════════
# 요약
# ═════════════════════════════════════════════════════════════
phase_summary() {
  step "Demo 완료"
  cat <<EOF

  ${GRN}✓${NC} 클러스터        : ${CLUSTER_NAME}
  ${GRN}✓${NC} Gateway URL     : ${GATEWAY_URL}
  ${GRN}✓${NC} 관리자 계정     : ${ADMIN_EMAIL} / ${ADMIN_PASSWORD}
  ${GRN}✓${NC} 등록 상품       : ${#PRODUCTS[@]} 개
  ${GRN}✓${NC} 지갑 충전       : ${WALLET_TOPUP_AMOUNT}원

  ${YLW}시연한 Saga 시나리오:${NC}
    [A] 잔액X + 재고X → 실패
    [B] 잔액O + 재고X → 실패 (StockReservationFailed)
    [C] 잔액O + 재고O → 성공 (Order CONFIRMED)
    [D] 재고 초과 주문 → 실패 (재고·잔액 롤백 검증)

EOF
}

# ═════════════════════════════════════════════════════════════
# 메인
# ═════════════════════════════════════════════════════════════
main() {
  local phase="${1:-all}"

  case "$phase" in
    all)
      phase_infra
      phase_app
      phase_scenario
      phase_summary
      ;;
    infra)      phase_infra ;;
    app)        phase_app ;;
    scenario)   phase_scenario; phase_summary ;;
    monitoring) phase_monitoring ;;
    *) err "phase: $phase (all|infra|app|scenario|monitoring)" ;;
  esac
}

main "$@"
