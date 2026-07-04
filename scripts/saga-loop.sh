#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
# snack24 Saga 반복 시연 — 잔액 소진 및 재고 복구 검증
#
# 시나리오:
#   1. 로그인해서 토큰 획득
#   2. 초기 지갑 잔액 / 재고 스냅샷
#   3. 잔액이 남아있는 동안 계속 주문 발행
#      → 잔액 부족으로 실패하는 시점까지 반복
#   4. 최종 지갑 / 재고 상태 확인
#      → 재고가 CONFIRMED 된 주문 수만큼만 차감됐는지 검증
#      → 실패한 주문의 재고 롤백이 제대로 됐는지 검증
#
# 실행:
#   bash scripts/saga-loop.sh
#
#   환경 변수:
#     MAX_ORDERS   (기본 100) — 안전장치, 이 수 이상은 중단
#     ORDER_QTY    (기본 5)   — 회당 주문 수량
#     PRODUCT_IDX  (기본 0)   — /v1/products 응답 배열의 몇 번째 상품 사용
#     SLEEP_MS     (기본 200) — 주문 간 간격 (ms)
# ─────────────────────────────────────────────────────────────

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# ═══ 설정 ═══
GATEWAY_URL="${GATEWAY_URL:-http://dev.snack24.local:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@demo.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-P@ssw0rd!}"

MAX_ORDERS="${MAX_ORDERS:-100}"
ORDER_QTY="${ORDER_QTY:-5}"
PRODUCT_IDX="${PRODUCT_IDX:-0}"
SLEEP_MS="${SLEEP_MS:-200}"

# ═══ 색상 ═══
RED='\033[0;31m'; GRN='\033[0;32m'; YLW='\033[1;33m'
BLU='\033[0;34m'; CYN='\033[0;36m'; NC='\033[0m'

log()   { echo -e "${BLU}[$(date +%H:%M:%S)]${NC} $*"; }
ok()    { echo -e "${GRN}✓${NC} $*"; }
warn()  { echo -e "${YLW}⚠${NC} $*"; }
fail()  { echo -e "${RED}✗${NC} $*"; }
step()  { echo -e "\n${YLW}▶ $*${NC}"; }

# ═══ 로그인 ═══
step "로그인 → 토큰 획득"
TOKEN=$(curl -s -X POST "${GATEWAY_URL}/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | jq -r '.accessToken // empty')

[[ -n "$TOKEN" ]] || { fail "로그인 실패 — demo.sh scenario 먼저 실행했나요?"; exit 1; }
ok "토큰 획득"

AUTH="Authorization: Bearer ${TOKEN}"

# ═══ 상품 선택 ═══
step "상품 목록 조회 및 대상 상품 선택"
PRODUCTS_JSON=$(curl -s "${GATEWAY_URL}/v1/products" -H "$AUTH")
PID=$(echo "$PRODUCTS_JSON" \
  | jq -r "(.content // .)[${PRODUCT_IDX}].id // (.content // .)[${PRODUCT_IDX}].productId")
# BigDecimal (예: "1200.00") 을 정수로 변환
PRICE=$(echo "$PRODUCTS_JSON" \
  | jq -r "((.content // .)[${PRODUCT_IDX}].unitPrice // (.content // .)[${PRODUCT_IDX}].price) | tonumber | floor")
NAME=$(echo "$PRODUCTS_JSON" \
  | jq -r "(.content // .)[${PRODUCT_IDX}].name")

[[ "$PID" != "null" && -n "$PID" ]] || { fail "상품 파싱 실패"; exit 1; }
ok "대상 상품: ${NAME} (id=${PID}, 단가=${PRICE})"
ok "회당 주문 수량: ${ORDER_QTY} → 회당 소요 금액: $((PRICE * ORDER_QTY))"

# ═══ 초기 상태 스냅샷 ═══
step "초기 상태 스냅샷"

wallet_balance() {
  # BigDecimal → 정수
  curl -s "${GATEWAY_URL}/v1/wallets" -H "$AUTH" \
    | jq -r '(.balance // 0) | tonumber | floor'
}

stock_quantity() {
  curl -s "${GATEWAY_URL}/v1/products/${PID}" -H "$AUTH" \
    | jq -r '(.availableQty // .stock // 0) | tonumber | floor'
}

INITIAL_BALANCE=$(wallet_balance)
INITIAL_STOCK=$(stock_quantity)
ok "초기 지갑 잔액: ${INITIAL_BALANCE}원"
ok "초기 재고 (${NAME}): ${INITIAL_STOCK}개"

# 재고 부족한 경우 경고
MAX_POSSIBLE_ORDERS=$((INITIAL_STOCK / ORDER_QTY))
if (( MAX_POSSIBLE_ORDERS < 2 )); then
  warn "재고가 너무 적음 (${INITIAL_STOCK} / ${ORDER_QTY} = ${MAX_POSSIBLE_ORDERS}회). demo.sh scenario 다시 돌려서 입고 필요."
fi

# ═══ 반복 주문 ═══
step "반복 주문 시작 (최대 ${MAX_ORDERS}회, ${SLEEP_MS}ms 간격)"

ORDER_BODY=$(cat <<EOF
{
  "items": [
    {"productId": ${PID}, "quantity": ${ORDER_QTY}}
  ]
}
EOF
)

SUCCESS_COUNT=0
FAIL_COUNT=0
ATTEMPT=0

start_time=$(date +%s)

while (( ATTEMPT < MAX_ORDERS )); do
  ATTEMPT=$((ATTEMPT + 1))

  RESPONSE=$(curl -s -w "\n__STATUS__%{http_code}" -X POST "${GATEWAY_URL}/v1/orders" \
    -H "$AUTH" -H "Content-Type: application/json" -d "$ORDER_BODY")
  STATUS=$(echo "$RESPONSE" | grep "__STATUS__" | sed 's/.*__STATUS__//')
  BODY=$(echo "$RESPONSE" | sed '/__STATUS__/d')

  ORDER_ID=$(echo "$BODY" | jq -r '.orderId // .id // empty')

  if [[ "$STATUS" =~ ^2 ]] && [[ -n "$ORDER_ID" && "$ORDER_ID" != "null" ]]; then
    # 주문 접수됨 — Saga 완료 후 최종 상태 확인
    sleep 0.5
    FINAL_STATUS=$(curl -s "${GATEWAY_URL}/v1/orders/${ORDER_ID}" -H "$AUTH" \
      | jq -r '.status // .orderStatus // "UNKNOWN"')

    case "$FINAL_STATUS" in
      CONFIRMED|COMPLETED|SUCCESS)
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        printf "${GRN}✓${NC} #%3d  order=%s  status=%s  " "$ATTEMPT" "$ORDER_ID" "$FINAL_STATUS"
        ;;
      CANCELED|FAILED|CANCELLED)
        FAIL_COUNT=$((FAIL_COUNT + 1))
        printf "${YLW}⚠${NC} #%3d  order=%s  status=%s  " "$ATTEMPT" "$ORDER_ID" "$FINAL_STATUS"
        ;;
      *)
        printf "${CYN}?${NC} #%3d  order=%s  status=%s (진행 중)  " "$ATTEMPT" "$ORDER_ID" "$FINAL_STATUS"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        ;;
    esac
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    REASON=$(echo "$BODY" | jq -r '.message // .detail // .error // "unknown"' 2>/dev/null || echo "unknown")
    printf "${RED}✗${NC} #%3d  HTTP %s  reason=%.60s  " "$ATTEMPT" "$STATUS" "$REASON"
  fi

  # 현재 지갑 잔액 표시
  CURRENT_BALANCE=$(wallet_balance)
  echo "  wallet=${CURRENT_BALANCE}원"

  # 잔액이 회당 소요보다 적으면 종료 (연속 실패 방지)
  REQUIRED=$((PRICE * ORDER_QTY))
  if (( CURRENT_BALANCE < REQUIRED )); then
    log "잔액 부족 (${CURRENT_BALANCE} < ${REQUIRED}). 추가 실패 방지 위해 종료."
    break
  fi

  # 간격 대기
  if (( SLEEP_MS > 0 )); then
    sleep "$(echo "scale=3; ${SLEEP_MS}/1000" | bc)"
  fi
done

end_time=$(date +%s)
elapsed=$((end_time - start_time))

# ═══ 최종 상태 ═══
step "최종 상태 확인 및 검증"

FINAL_BALANCE=$(wallet_balance)
FINAL_STOCK=$(stock_quantity)

echo
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "  총 시도       : %d회\n" "$ATTEMPT"
printf "  ${GRN}성공${NC}          : %d회 (CONFIRMED)\n" "$SUCCESS_COUNT"
printf "  ${YLW}실패${NC}          : %d회 (CANCELED)\n" "$FAIL_COUNT"
printf "  소요 시간     : %d초\n" "$elapsed"
echo
printf "  지갑 잔액     : %d원 → %d원 (차감: %d원)\n" \
  "$INITIAL_BALANCE" "$FINAL_BALANCE" "$((INITIAL_BALANCE - FINAL_BALANCE))"
printf "  재고 (%s) : %d개 → %d개 (차감: %d개)\n" \
  "$NAME" "$INITIAL_STOCK" "$FINAL_STOCK" "$((INITIAL_STOCK - FINAL_STOCK))"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ═══ 검증 ═══
step "Saga 정합성 검증"

EXPECTED_WALLET_DEBIT=$((SUCCESS_COUNT * PRICE * ORDER_QTY))
ACTUAL_WALLET_DEBIT=$((INITIAL_BALANCE - FINAL_BALANCE))

EXPECTED_STOCK_DEDUCTION=$((SUCCESS_COUNT * ORDER_QTY))
ACTUAL_STOCK_DEDUCTION=$((INITIAL_STOCK - FINAL_STOCK))

echo
echo "[지갑 차감 정합성]"
echo "  예상: ${SUCCESS_COUNT}회 × ${PRICE}원 × ${ORDER_QTY} = ${EXPECTED_WALLET_DEBIT}원"
echo "  실제: ${ACTUAL_WALLET_DEBIT}원"
if [[ "$EXPECTED_WALLET_DEBIT" == "$ACTUAL_WALLET_DEBIT" ]]; then
  ok "일치 — 실패한 주문의 지갑 차감이 롤백됨"
else
  fail "불일치 — 실패한 주문에서 지갑 차감이 잘못 반영됐거나 롤백 실패"
fi

echo
echo "[재고 차감 정합성]"
echo "  예상: ${SUCCESS_COUNT}회 × ${ORDER_QTY} = ${EXPECTED_STOCK_DEDUCTION}개"
echo "  실제: ${ACTUAL_STOCK_DEDUCTION}개"
if [[ "$EXPECTED_STOCK_DEDUCTION" == "$ACTUAL_STOCK_DEDUCTION" ]]; then
  ok "일치 — 실패한 주문의 재고 예약이 롤백됨"
else
  fail "불일치 — 재고 롤백 실패 (Saga 보상 로직 점검 필요)"
fi

echo
echo "   실패한 주문이 지갑·재고에 아무 흔적을 남기지 않으면 = 보상 트랜잭션 정상 동작."
