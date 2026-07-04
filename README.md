# snack24 — 사내 간식 관리 플랫폼

> 실무에서 다루기 어려운 MSA · Saga · Kubernetes · GitOps
> 한 도메인 안에서 구현하는 것을 목표로 진행하였습니다.

![CI](https://github.com/jinwoo827/snack24/actions/workflows/ci.yml/badge.svg)
![Build & Push](https://github.com/jinwoo827/snack24/actions/workflows/build-push.yml/badge.svg)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![Kubernetes](https://img.shields.io/badge/Kubernetes-k3d-blue)

---

| 항목        | 내용 |
|-----------|------|
| **구성**    | MSA 6개 서비스 + Saga Orchestrator + Outbox 패턴을 Kubernetes 위에 GitOps 로 배포 |
| **주요 기술** | Java 21, Spring Boot 3.3, MariaDB, Kafka(KRaft), Redis, k3d, Kustomize, GitHub Actions, GHCR, Prometheus, Grafana |
| **배포 형태** | k3d 로컬 클러스터 + dev/prod overlay 분리, HPA + 무중단 롤링 업데이트 |
| **CI/CD** | PR 검증 → 이미지 빌드/푸시(GHCR) → 매니페스트 자동 bump → 로컬 apply 흐름 |

---

## 목차

1. [Why this project — 도메인 선정과 목표](#why-this-project)
2. [아키텍처 개요](#아키텍처-개요)
3. [핵심 기술 결정](#핵심-기술-결정)
   - [Saga Orchestration — 왜 오케스트레이션인가](#1-saga-orchestration)
   - [Outbox Pattern — 이벤트 유실 방지](#2-outbox-pattern)
   - [동시성 제어 — 지갑과 재고의 서로 다른 전략](#3-동시성-제어)
   - [Multi-tenant — Trusted Header 로 인증 위임](#4-multi-tenant--trusted-header)
   - [멱등성 — 이중 방어](#5-멱등성)
4. [관측성 (Observability)](#관측성)
5. [CI/CD 파이프라인](#cicd-파이프라인)
6. [Kubernetes 배포 전략](#kubernetes-배포-전략)
7. [로컬 실행 방법](#로컬-실행-방법)

---

## Why this project

### 프로젝트 도메인 선정 이유

B2B 기업복지 SaaS 도메인 습득을 목적으로 두었습니다.

- **다중 테넌시** — 회사(company) 단위 데이터 격리
- **분산 트랜잭션** — 주문 · 결제(지갑) · 재고가 각각 다른 서비스
- **비동기 이벤트** — 주문 확정 → 알림 발송 등
- **관리자 · 사용자 두 페르소나** — RBAC 필요성

---

## 아키텍처 개요

### 시스템 구성도

```mermaid
graph TB
    subgraph "사용자"
        Client[Client]
    end

    subgraph "Edge"
        GW[api-gateway<br/>Spring Cloud Gateway<br/>JWT 검증 · 신뢰 헤더 주입]
    end

    subgraph "Services"
        ID[identity<br/>인증 · 회사/멤버]
        CAT[catalog<br/>상품 · 재고]
        ORD[order<br/>주문 · Saga Orchestrator]
        BIL[billing<br/>지갑 · 결제]
        NOT[notification-worker<br/>Kafka Consumer<br/>알림 발송]
    end

    subgraph "Infra"
        MDB[(MariaDB<br/>서비스별 스키마)]
        RDS[(Redis<br/>Refresh · 멱등성)]
        KFK[Kafka KRaft<br/>Event Bus]
    end

    Client -->|HTTPS| GW
    GW --> ID
    GW --> CAT
    GW --> ORD
    GW --> BIL

    ID <--> MDB
    ID <--> RDS
    CAT <--> MDB
    ORD <--> MDB
    BIL <--> MDB
    NOT <--> RDS

    ORD -->|동기 REST<br/>재고 확인| CAT
    ORD -->|Command Event| KFK
    KFK -->|Response Event| ORD
    CAT -->|이벤트 발행| KFK
    BIL -->|이벤트 발행| KFK
    KFK --> NOT
```

### 서비스별 역할

| 서비스 | Port | 책임                                                         | 저장소 |
|--------|------|------------------------------------------------------------|--------|
| **api-gateway** | 8000 | 라우팅 · JWT 검증 · 신뢰할수 있는 헤더 (X-Company-Id/Member-Id/Role) 주입 | — |
| **identity** | 8001 | 인증 · JWT 발급/재발급 · 회사/회원 CRUD                               | MariaDB, Redis |
| **catalog** | 8002 | 상품/재고 CRUD · 재고 예약(Saga step)                              | MariaDB |
| **order** | 8003 | 주문 · **Saga Orchestrator** · Outbox 발행                     | MariaDB |
| **billing** | 8004 | 지갑 · 차감(Saga step) · 롤백                                    | MariaDB |
| **notification-worker** | 8005 | Kafka Consumer · 알림 발송                                     | Redis (멱등성) |

## 핵심 기술 결정

### 1. Saga Orchestration

Choreography vs Orchestration 을 놓고 오케스트레이션 선택.

| 항목 | Choreography | **Orchestration (선택)** |
|------|--------------|------------------------|
| 결합도 | 낮음 | 중간 (오케스트레이터가 흐름 소유) |
| 흐름 가시성 | ❌ 분산됨 | ✅ 한 곳에서 관찰 |
| 실패 처리 | 각 서비스가 알아서 | 오케스트레이터가 보상 트랜잭션 지시 |

실무에서 주문시에 복잡하게 동작하는 주문 도메인에 대해 튜러블슈팅시에 어느단계, 어떤 사유로 실패되었는지
추적이 용이한점으로 결정

#### 주문 Saga 흐름

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant O as order (Orchestrator)
    participant OT as Outbox Table
    participant K as Kafka
    participant CT as catalog
    participant B as billing

    C->>O: POST /orders
    O->>O: Order 생성 (PENDING)
    O->>OT: ReserveStockCommand 저장 (트랜잭션 내)
    Note over O,OT: 커밋

    OT-->>K: MessageRelay 가 이벤트 발행
    K->>CT: ReserveStockCommand
    CT->>CT: 재고 차감 (비관 락)
    CT->>K: StockReserved

    K->>O: StockReserved
    O->>OT: DebitWalletCommand 저장
    OT-->>K: 발행
    K->>B: DebitWalletCommand
    B->>B: 지갑 차감 (조건부 UPDATE)
    B->>K: WalletDebited

    K->>O: WalletDebited
    O->>O: Order CONFIRMED
    O->>OT: OrderConfirmed 저장
    OT-->>K: 발행
    K->>C: (via notification) 알림

    Note over O,B: 실패 시 → CompensationEvent 로 역방향 롤백
```

**보상 트랜잭션 예:**
-  "재고 수량 부족시" → `StockReservationFailed` → 주문 CANCELED (지갑의 잔액 차감 없음)
-  "지갑의 잔액 부족시" → `WalletDebitFailed` → `ReleaseStockCommand` 발행 → 재고 복구 → 주문 CANCELED

---

### 2. Outbox Pattern

*"DB 커밋은 됐는데 Kafka 발행 실패"* 또는 *"Kafka 발행은 됐는데 DB 롤백"* 같은
**이중 쓰기(dual-write) 문제** 를 방지하기 위함.

#### 흐름

```mermaid
graph LR
    A[비즈니스 로직] -->|1. 트랜잭션 내| B[(DB - domain table)]
    A -->|1. 같은 트랜잭션| C[(DB - outbox_events)]
    B -.->|커밋| B
    C -.->|커밋| C
    D[MessageRelay<br/>Scheduled] -->|2. polling| C
    D -->|3. 발행| E[Kafka]
    D -->|4. processed=true| C
```

---

### 3. 동시성 제어

## 비관적 락

#### 지갑 — 조건부 UPDATE

```java
    @Modifying(clearAutomatically = true)
    @Query("update Wallet w " +
            "   set w.balance = w.balance - :amount " +
            " where w.companyId = :companyId " +
            "   and w.balance >= :amount "
    )
    int debitIfSufficient(@Param("companyId") Long companyId,
                          @Param("amount") BigDecimal amount);
```
```sql
-- 성공 시 update된 행 수 = 1, 실패 시 0
UPDATE wallet
   SET balance = balance - ?,
       updated_at = NOW()
 WHERE company_id = ?
   AND balance >= ?      -- ★ 잔액 검증을 UPDATE 조건에
```

#### 재고 — 비관 락 + productId 정렬

```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId in :productIds")
    List<Stock> findAllByProductIdInForUpdate(@Param("productIds") List<Long> productIds);
    // SELECT ... FROM products WHERE id IN (?) ORDER BY id FOR UPDATE
```

- 재고는 잔액과 달리 *"상품들의 재고 차감이 부분 성공을"* 허용 하지 않도록 구현

---

### 4. Multi-tenant — Trusted Header

Gateway 에서 JWT 검증 후 **뒤단 서비스에 신뢰 헤더** 로 전달:

```
X-Company-Id: 123
X-Member-Id: 456
X-Member-Role: ADMIN
```

Spring 의 `HandlerMethodArgumentResolver` 로 `@Caller` 어노테이션 커스텀:
각각의 서비스들은 인증에 대한 신경을 쓰지 않고 api-gateway 주입해준 헤더 정보를 쉽게 사용

```java
public class MemberController {
  @GetMapping("/me")
  public MemberDto me(@Caller CallerContext caller) {
    return memberService.findById(caller.memberId());
  }
}
```

---
## 5. Saga 시나리오별 상세 흐름

### 5.1 주문 성공

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant O as order
    participant K as Kafka
    participant CT as catalog
    participant B as billing
    participant N as notification

    C->>O: POST /v1/orders
    Note over O: orders INSERT (PENDING)<br/>saga_instance INSERT (STARTED)<br/>outbox RESERVE_STOCK_COMMAND
    O-->>C: 202 Accepted {orderId, sagaId}

    O->>K: RESERVE_STOCK_COMMAND
    K->>CT: consume
    Note over CT: stocks UPDATE locked_qty += n<br/>(비관 락 + productId 정렬)
    CT->>K: STOCK_RESERVED

    K->>O: consume STOCK_RESERVED
    Note over O: saga_instance UPDATE (STOCK_RESERVED)<br/>outbox DEBIT_WALLET_COMMAND
    O->>K: DEBIT_WALLET_COMMAND

    K->>B: consume
    Note over B: wallets 조건부 UPDATE (balance -= amt)<br/>wallet_transactions INSERT (DEBIT)
    B->>K: WALLET_DEBITED

    K->>O: consume WALLET_DEBITED
    Note over O: saga_instance UPDATE (COMPLETED)<br/>orders UPDATE (CONFIRMED, confirmed_at)<br/>outbox ORDER_CONFIRMED
    O->>K: ORDER_CONFIRMED

    K->>N: consume
    Note over N: 주문 완료 알림 발송
```

### 5.2 재고 부족 실패

```mermaid
sequenceDiagram
    autonumber
    participant O as order
    participant K as Kafka
    participant CT as catalog
    participant N as notification

    Note over O: 주문 접수 (동일)
    O->>K: RESERVE_STOCK_COMMAND
    K->>CT: consume
    Note over CT: SELECT FOR UPDATE<br/>재고 부족 → rollback
    CT->>K: STOCK_RESERVATION_FAILED (reason=insufficient stock)

    K->>O: consume
    Note over O: saga_instance UPDATE (FAILED)<br/>orders UPDATE (CANCELED, canceled_at, cancel_reason)<br/>outbox ORDER_CANCELED
    O->>K: ORDER_CANCELED
    K->>N: 주문 취소 알림

```

### 5.3 지갑 잔액 부족 실패 (보상 트랜잭션)

```mermaid
sequenceDiagram
    autonumber
    participant O as order
    participant K as Kafka
    participant CT as catalog
    participant B as billing
    participant N as notification

    Note over O: 주문 접수
    O->>K: RESERVE_STOCK_COMMAND
    K->>CT: consume → stocks locked_qty += n
    CT->>K: STOCK_RESERVED

    K->>O: consume
    Note over O: saga (STOCK_RESERVED)
    O->>K: DEBIT_WALLET_COMMAND

    K->>B: consume
    Note over B: 조건부 UPDATE 실패<br/>(balance < amt)
    B->>K: WALLET_DEBIT_FAILED

    rect rgba(255, 200, 200, 0.3)
        Note over O,CT: ⚡ 보상 트랜잭션 시작
        K->>O: consume WALLET_DEBIT_FAILED
        Note over O: saga (COMPENSATING)<br/>outbox RELEASE_STOCK_COMMAND
        O->>K: RELEASE_STOCK_COMMAND

        K->>CT: consume
        Note over CT: stocks UPDATE locked_qty -= n<br/>(재고 복구)
        CT->>K: STOCK_RELEASED

        K->>O: consume STOCK_RELEASED
        Note over O: saga (FAILED)<br/>orders UPDATE (CANCELED)<br/>outbox ORDER_CANCELED
    end

    O->>K: ORDER_CANCELED
    K->>N: 주문 취소 알림
```

---

### 6. 멱등성

이벤트 멱등성 보장:

#### Level 1 — processed_messages 테이블

각 서비스가 소유:

```sql
CREATE TABLE processed_messages (
  saga_id     VARCHAR(64),
  event_type  VARCHAR(64),
  processed_at TIMESTAMP,
  PRIMARY KEY (saga_id, event_type)
);
```

---

## 관측성

### Metrics — 인프라

#### 자동 수집 (Actuator)

- JVM (Heap, GC, Thread)
- HTTP request rate/latency (p50/p95/p99)
- Tomcat thread pool
- DB connection pool

---

## CI/CD 파이프라인

### 워크플로 구성

```mermaid
graph LR
    A[PR / push] --> B{워크플로}
    B -->|PR| C[ci.yml<br/>빌드 + 테스트]
    B -->|main push| D[build-push.yml<br/>path filter → 변경 서비스만 빌드]
    D --> E[GHCR<br/>multi-platform 이미지]
    D --> F[deploy-dev.yml<br/>overlay 이미지 sha bump]
    B -->|v* tag| G[deploy-prod.yml<br/>승인 대기]
    G -->|Approve| H[overlay semver bump]
    F --> I[git commit skip ci]
    H --> I
    I --> J[로컬 kubectl apply]
```

### 워크플로별 역할

| Workflow | 트리거 | 동작 |
|----------|--------|------|
| `ci.yml` | PR / main push | Gradle 빌드 + 테스트, JUnit 리포트 PR Checks 노출 |
| `build-push.yml` | main push, `v*` tag | Path filter → 변경 서비스만 · multi-platform(amd64+arm64) · GHCR 푸시 |
| `deploy-dev.yml` | build-push 성공 시 자동 | `overlays/dev/kustomization.yaml` 이미지 태그 sha bump (bot commit) |
| `deploy-prod.yml` | `v*` tag + **수동 승인** | GitHub Environment Required Reviewers · prod overlay semver bump |

---

## Kubernetes 배포 전략

### 매니페스트 구조

```
infra/k8s/
├── base/                        # 환경 무관 공통
│   ├── namespace.yaml
│   ├── mariadb/                 # StatefulSet + Headless Service + PVC
│   ├── redis/, kafka/
│   ├── apps/                    # 6개 서비스 Deployment + Service
│   ├── secrets/
│   └── ingress/                 # NGINX Ingress
└── overlays/
    ├── dev/                     # replicas=2, latest 태그, DEBUG 로그
    └── prod/                    # replicas=2, semver 태그, HPA 활성
```
---

## 로컬 실행 방법

### 전제 조건

- Docker Desktop (12GB 이상 권장)
- Java 21, Gradle
- `k3d`, `kubectl`, `helm`, `jq`, `hey`

### Quick Start

```bash
# 1. 로컬에 인프라 세팅 및 애플리케이션 Docker 이미지 빌드 및 k3d 이미지 import 후 시연  
  scripts/demo.sh
  
# 2. 주문 부하 시연
  MAX_ORDERS=500 ORDER_QTY=1 SLEEP_MS=10 bash scripts/saga-loop.sh  
```

### 모니터링
```bash
  kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
  kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80   
```


