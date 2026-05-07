# snack24

> 사내 간식 구독 관리 플랫폼 — B2B SaaS, 마이크로서비스, Saga 분산 트랜잭션 학습 프로젝트

## 무엇을 만드는가

회사가 사무실에 둘 간식을 정기 구독하고, 직원이 픽업하면 회사 잔액에서 차감되며, 관리자는 부서/기간별 사용량을 대시보드에서 확인하는 시스템.

위펀(WeFun)의 **스낵24** 서비스 흐름을 모티브로, **B2B 멀티 테넌트 SaaS** 의 핵심 백엔드 문제를 직접 풀어본다.

## 왜 이 프로젝트를 만드나

9년차 자바 백엔드 개발자의 이직 포트폴리오. **기존 실무에서 깊게 다루지 못한 4가지 스택**을 하나의 도메인 안에서 의도적으로 사용:

1. **QueryDSL** — 관리자 대시보드의 동적 검색·집계
2. **GraphQL** — 관리자 BFF 한 곳에서 다중 도메인 조회 1요청 처리
3. **Docker / Kubernetes** — 6개 서비스의 컨테이너화 + k3d 로컬 클러스터 + 무중단 롤링 업데이트
4. **CI/CD** — GitHub Actions 단일 파이프라인으로 빌드·테스트·이미지 푸시·매니페스트 적용

도메인 모델링과 서비스 분리 근거는 [docs/adr/0001-msa-vs-modular-monolith.md](docs/adr/0001-msa-vs-modular-monolith.md) 참조.

## 아키텍처

```
[Admin / Member]
       │
       ▼
┌─────────────┐
│ api-gateway │  Spring Cloud Gateway, JWT 검증
└──────┬──────┘
       │
       ├──► identity-service           (8001)  회사 / 부서 / 직원 / JWT
       ├──► catalog-service            (8002)  상품 / 구독 플랜
       ├──► order-service              (8003)  주문 / 픽업 / Saga Orchestrator
       └──► billing-service            (8004)  회사 지갑 / 사용 기록 / 청구서

       (Kafka)
       ↓
   notification-worker                 (8005)  이벤트 → 알림 (메일/슬랙)
```

## 기술 스택

| 영역 | 선택 |
|---|---|
| 언어 / 런타임 | Java 21 |
| 프레임워크 | Spring Boot 3.3.5 |
| ORM | Spring Data JPA + QueryDSL 5.1 |
| RDBMS | MariaDB 11 |
| 캐시 / 락 | Redis 7 |
| 메시징 | Kafka 3.7 (KRaft 모드) |
| API | REST + GraphQL (관리자 BFF) |
| 인증 | JWT + Spring Security |
| 컨테이너 | Docker, Docker Compose |
| 오케스트레이션 | Kubernetes (로컬은 k3d) |
| CI/CD | GitHub Actions |
| 빌드 | Gradle 8 (Groovy DSL) 멀티 모듈 |

## 모듈 구성

```
snack24/
├── common/
│   ├── snowflake/         분산 ID 생성기
│   ├── event/             도메인 이벤트 envelope + EventType + payload
│   ├── data-serializer/   Jackson 단일 진입점
│   └── outbox-relay/      Outbox + Saga Orchestrator (Week 3 구현)
├── service/
│   ├── identity/          회사·부서·직원·JWT
│   ├── catalog/           상품·구독 플랜
│   ├── order/             주문·픽업·Saga Orchestrator
│   ├── billing/           회사 지갑·사용 기록·청구
│   ├── notification-worker/  Kafka 이벤트 컨슈머
│   └── api-gateway/       JWT 검증 + 라우팅 + GraphQL BFF
├── infra/
│   ├── docker/            docker-compose
│   └── k8s/               Kustomize base + dev/prod overlay
├── load-test/             k6 시나리오
└── docs/
    └── adr/               Architecture Decision Records
```

## 진행 로드맵 (8주)

| 주차 | 내용 | 산출물 |
|---|---|---|
| 1 | 모노레포 골격 + identity-service | identity 단독 동작, JWT 발급/검증 |
| 2 | catalog + order (REST + JPA + QueryDSL) | 상품/주문 CRUD + 동적 검색 |
| 3 | billing + Kafka + Saga Orchestrator | 분산 트랜잭션 흐름 통합 테스트 |
| 4 | api-gateway + JWT + notification-worker | 게이트웨이 + 비동기 알림 |
| 5 | GraphQL BFF + DataLoader (N+1 해소) | 관리자 대시보드 1요청 다도메인 조회 |
| 6 | Dockerize + docker-compose + k6 부하 테스트 | p95 응답시간 측정 결과 |
| 7 | Kubernetes (k3d) + Deployment/Service/HPA | 무중단 롤링 시연 |
| 8 | GitHub Actions CI/CD + 모니터링 + 정리 | 푸시 → 자동 배포 동영상 |

## 로컬 실행

> 본 단계는 Week 6 이후 의미 있음. Week 1~5 동안은 각 서비스를 개별 `bootRun` 으로 실행.

```bash
# Week 1 산출물 빌드 검증
./gradlew build

# 개별 서비스 실행 (Week 1)
./gradlew :service:identity:bootRun
```

## ADR 인덱스

- [0001 — MSA vs 모듈러 모놀리스](docs/adr/0001-msa-vs-modular-monolith.md)
- 0002 — Saga: Choreography vs Orchestration *(Week 3)*
- 0003 — GraphQL BFF의 범위 결정 *(Week 5)*

## 참고 / 출처

- `common/snowflake`, `common/event` 패턴은 별도 학습 프로젝트 `kuke-board`(인프런 강의 자료)의 구조를 참조하여 본 프로젝트의 도메인·패키지 규칙에 맞게 재작성.
- 본 프로젝트 도메인 모델·MSA 분리·Saga 설계·Kubernetes 매니페스트는 본인이 직접 설계.
