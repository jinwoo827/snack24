# ADR 0002 — 분산 트랜잭션: Saga (Orchestration)

**상태:** Accepted
**날짜:** 2026-05-08
**관련:** [ADR 0001 — MSA vs 모듈러 모놀리스](0001-msa-vs-modular-monolith.md), [Saga 흐름 상세](../saga-order-flow.md)

## 1. 컨텍스트

[ADR 0001](0001-msa-vs-modular-monolith.md) 결정에 따라 4개 도메인(identity / catalog / order / billing)이 별도 서비스로 분리됨. 각 서비스는 자체 DB(MariaDB) 보유.

주문 흐름은 다음 3개 서비스에 트랜잭션이 걸쳐 있음:

```
order: 주문 PENDING 저장
catalog: 재고 예약 (locked_qty++)
billing: 잔액 차감 (balance -=)
order: 주문 CONFIRMED 확정
```

이 흐름의 일관성을 어떻게 보장할 것인가가 본 ADR의 주제.

## 2. 후보안

### 2.1 2PC (Two-Phase Commit) — 검토 후 기각

XA 트랜잭션으로 묶기. 이론적으론 ACID 보장.

**기각 사유:**
- 코디네이터가 단일 장애점
- 모든 참여자가 prepare 단계에서 락을 잡고 대기 → 트래픽 증가 시 처리율 급락
- 운영에서 거의 사용되지 않는 패턴 — 면접 답변 가치도 낮음

### 2.2 Choreography (안무형 Saga)

각 서비스가 이벤트를 듣고 자기 알아서 다음 단계 트리거. 중앙 조정자 없음.

**장점:** 결합도 낮음, 단순한 첫 구현, kuke-board에서 일부 패턴 학습됨
**단점:** 흐름 추적이 코드 따라다니기 — 5단계 넘어가면 운영 시 "지금 어디서 막혔지?" 답하기 어려움

### 2.3 Orchestration (조정형 Saga) — **채택**

한 서비스가 코디네이터 역할로 단계별 명령을 발행하고 응답을 받음.

**장점:** 흐름이 한 곳에 집중 → 디버깅·로깅·면접 답변 용이. saga_instance 테이블로 상태 영속화 가능.
**단점:** 코디네이터에 비즈니스 로직 집중 → "살찐 코디네이터" 경계 필요.

## 3. 결정

**Orchestration 채택. 코디네이터는 order 서비스.**

[Saga 흐름 상세](../saga-order-flow.md) 에 정상/보상 시퀀스, 멱등성/타임아웃/DLQ 정책 명시.

## 4. 결정 근거

### 4.1 도메인이 좁다 (3 서비스 협력)

Choreography의 결합도 낮음 장점은 "많은 서비스가 협력할 때" 빛난다. 본 프로젝트의 주문 Saga는 3개 서비스만 관여하므로 그 가치가 미미하다. 반면 흐름 추적이 한 곳에 보이는 Orchestration의 장점은 즉시 효과 있음.

### 4.2 본 프로젝트 학습/포트폴리오 목적

[ADR 0001](0001-msa-vs-modular-monolith.md) 에서 명시한 대로 본 프로젝트는 학습/포트폴리오를 1차 목적으로 함. **kuke-board(별도 학습 프로젝트)에서는 Choreography 풍의 이벤트 패턴을 이미 다뤘으므로**, 본 프로젝트는 Orchestration 으로 가서 두 패턴 모두 다뤄본 경험으로 어필 폭을 넓히는 게 합리적.

### 4.3 sagaInstance 영속화로 재시작 복구

코디네이터 죽으면 진행 중 saga가 멈추는 문제는 **saga_instance 테이블 + outbox 패턴 조합**으로 해결. 이는 Orchestration이라서 가능한 접근(Choreography는 상태가 메시지 흐름에 분산되어 있어 "어디까지 갔나"를 한 곳에서 답하기 어려움).

### 4.4 부수 결정 — 본 ADR이 함의하는 것들

| 결정 | 내용 | 근거 |
|---|---|---|
| Stock 어그리게잇 분리 | catalog 서비스 안에서 Product와 Stock을 별도 어그리게잇으로 분리 | 인기 상품 락 분산, 회사별 재고 확장 여지 |
| 잔액 부족은 Saga 보상으로만 | 사전 동기 체크 X. race condition을 일부러 보상 트랜잭션으로 푸는 학습 | 분산 시스템의 정석을 흉내내는 게 학습 목적 |
| 월 청구서는 Saga 미사용 | 월 1회 배치 + 멱등 처리로 충분 | 학습 가치는 온라인 트랜잭션에 집중 |
| PG 연동은 어댑터 인터페이스 + Mock | billing 안 `PaymentGateway` 인터페이스 + `MockPaymentGateway` | 확장 포인트 설계 시연 |
| 토픽: 도메인별 단일 토픽 3개 | 명령과 이벤트 한 토픽에 공존, 봉투 type 필드와 sagaId 헤더로 구분 | 토픽 12개로 분리 시 학습 가치 대비 운영 부담 큼 |

## 5. 인정하는 트레이드오프

- **코디네이터 살찜 위험.** order 서비스에 SagaOrchestrator 책임이 모임. → 도메인 로직(Order 어그리게잇)과 Saga 흐름 제어를 별 클래스로 명확히 분리. SagaOrchestrator는 "다음 단계 결정" 외 비즈니스 로직 X.
- **단일 saga 종류만 다룬다.** 본 프로젝트는 주문 Saga 하나만 구현. 여러 종류 Saga가 등장하면 SagaOrchestrator가 비대해질 위험 — 그 시점이 오면 saga 종류별 클래스 분리.
- **Eventually consistent.** 직원 입장에서 "픽업 완료" 응답을 즉시 받지 못함 (Saga가 비동기로 진행). UX는 클라이언트에서 "처리 중" 상태를 폴링하거나 WebSocket으로 보완. 본 프로젝트는 폴링 API만 제공.

## 6. "정답"이 아닌 "현재 컨텍스트의 적합한 선택"

본 결정은 **본 프로젝트 도메인 크기와 학습 목적에 적합한 선택**이지 "Saga = Orchestration이 정답" 이라는 일반화 주장이 아니다.

도메인이 6개 이상 서비스로 확장되거나 흐름이 비선형적(분기 많음)으로 변하면 Choreography 또는 두 패턴 혼용을 다시 검토해야 한다. 그 시점에 신규 ADR 작성.

## 7. 검증 계획 (Week 3)

- [ ] saga_instance 테이블 + 상태 머신 구현 후 단위 테스트
- [ ] 정상 흐름 통합 테스트 (Testcontainers + Kafka)
- [ ] 보상 흐름 통합 테스트 (잔액 부족 시뮬레이션)
- [ ] 멱등성 검증 — 같은 메시지 2회 발행해도 결과 동일
- [ ] 타임아웃 검증 — catalog 응답 지연 시 보상 트리거
- [ ] 코디네이터 재시작 후 saga 복구 시나리오 (Docker 컨테이너 강제 종료 후 재기동)
