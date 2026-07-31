# chookjibupUser_BE (Festival Flow AI 사용자 서버)

`demoAdmin`과 같은 스타일(Spring Boot 4.1.0 / Java 21)로 만들었지만, **관리자 서버는 코드 스타일
참고용일 뿐 실제로 호출하지 않습니다.** 축제 데이터는 여러분이 만든 Python 파이프라인
(`julcut_data_pipeline`)이 채워놓은 **로컬 Postgres에 이 서버가 JPA로 직접 연결**해서 읽습니다.

```
julcut_data_pipeline (Python)     chookjibupUser_BE (이 서버, Spring)
        │ 채움                          │ 같은 DB에 직접 연결(JPA)
        ▼                               ▼
        └────────── 같은 로컬 Postgres ──────────┘
        테이블: users, festivals, festival_wishlist, ... (schema.sql)
```

이 서버가 실제로 쓰는 테이블은 8개입니다.
- `festivals` — 읽기 전용 (파이프라인이 채움)
- `users` — 카카오 로그인 계정 (이 서버가 씀)
- `festival_wishlist` — 찜 (이 서버가 씀)
- `festival_roadmap`, `roadmap_icon_placement`, `roadmap_icon_type` — 로드맵 (읽기 전용, 관리자 쪽이 채움)
- `booth_info` — 부스 정보 (읽기 전용, 관리자 쪽이 채움)
- `booth_congestion`, `festival_congestion` — 혼잡도 이력 (읽기 전용, 관리자/운영자/알바생이 채움)

## ⚠️ 실행 전에 반드시 먼저 하실 일: `users` 테이블 마이그레이션

파이프라인이 만든 `users` 테이블은 원래 아이디/비밀번호 방식으로 설계돼 있어서, 카카오 로그인
전용 컬럼(`kakao_id`, `email`, `profile_image_url`)이 없고 `nickname` 대신 `name`이었습니다.
같이 드린 **`schema.sql`로 교체**하고 파이프라인을 한 번 더 돌리면(`python3 main.py`) 자동으로
안전하게 이관됩니다.

- 이미 `login_id`/`password_hash`/`name`으로 가입된 행이 있어도 **데이터 손실 없이** 마이그레이션됩니다.
  (`name` 값은 `nickname`으로 그대로 옮겨지고, `login_id`/`password_hash`는 제거됩니다.)
- 실제로 로컬 Postgres에 옛날 구조로 테이블을 만들어두고 이 `schema.sql`을 다시 실행해서
  데이터가 안 지워지고 옮겨지는 것까지 확인했습니다.
- `kakao_id`는 DB 레벨에서는 nullable로 뒀습니다(레거시 행 때문). "카카오 로그인 시 반드시
  있어야 한다"는 이 서버(`UserAccount.createFromKakao`)에서 애플리케이션 레벨로 보장합니다.

적용 방법:
```bash
# julcut_data_pipeline 리포지토리에서
cp <받으신 새 schema.sql 경로> ./schema.sql
python3 main.py   # schema_loader가 자동으로 마이그레이션까지 적용합니다
```

## 폴더 구조 (도메인별로 서비스 분리, 컨트롤러는 호출만)

각 도메인(`festival`, `wishlist`, `user`, `roadmap`, `booth`, `congestion`)의 서비스는
**자기 도메인 리포지토리만** 다루고 서로의 존재를 모릅니다. 두 도메인 이상을 엮어야 하는
화면(축제 상세에 로드맵·부스·혼잡도 합치기, 목록에 찜 여부 표시 등)은 별도의 `application`
계층 조합 서비스(`UserFestivalService`, `UserWishlistService`)가 담당합니다. 컨트롤러는
그 서비스 하나만 호출합니다.

```
chookjibupUser_BE/
├── auth/                          # 카카오 로그인 + 우리 서버 자체 JWT
│   ├── command/application/KakaoLoginService.java
│   ├── command/infrastructure/{JwtTokenProvider, kakao/*}
│   └── support/{JwtAuthenticationFilter, UserPrincipal}
├── user/                           # UserAccount 엔티티 + Repository (users 테이블)
├── festival/                        # ── festival 도메인 (순수, wishlist를 모름) ──
│   ├── Festival.java                    # 엔티티 (festivals 테이블, 읽기전용)
│   ├── FestivalRepository.java           # Spring Data JPA
│   ├── FestivalQueryService.java          # 목록 조회 / id로 조회 / 존재 확인
│   └── dto/                                # FestivalSummaryView, FestivalPageView, ...
├── wishlist/                        # ── wishlist 도메인 (순수, festival을 모름) ──
│   ├── FestivalWishlist.java            # 엔티티 (festival_wishlist 테이블)
│   ├── WishlistRepository.java           # Spring Data JPA
│   ├── WishlistCommandService.java        # 찜 토글
│   ├── WishlistQueryService.java           # 찜 목록/찜 여부 조회 (festivalId만 반환)
│   └── dto/                                 # WishlistEntryView (festival 상세 정보 없음)
├── roadmap/                          # ── roadmap 도메인 (순수) ──
│   ├── FestivalRoadmap.java              # 엔티티 (festival_roadmap, 1:1)
│   ├── RoadmapIconPlacement.java          # 엔티티 (roadmap_icon_placement)
│   ├── RoadmapIconType.java                # 엔티티 (roadmap_icon_type, 아이콘 카탈로그)
│   ├── *Repository.java                     # roadmap_type은 네이티브 쿼리로 ::text 캐스팅
│   ├── RoadmapQueryService.java               # 축제 로드맵 + 아이콘 목록 조합(도메인 내부에서만)
│   └── dto/                                     # RoadmapView, RoadmapIconView
├── booth/                             # ── booth 도메인 (순수, congestion을 모름) ──
│   ├── BoothInfo.java                     # 엔티티 (booth_info)
│   ├── BoothInfoRepository.java            # Spring Data JPA
│   ├── BoothQueryService.java                # 축제의 부스 목록 조회
│   └── dto/BoothView.java
├── congestion/                        # ── congestion 도메인 (순수, booth/festival을 모름) ──
│   ├── BoothCongestion.java               # 엔티티 (booth_congestion, congestion_level 비매핑)
│   ├── FestivalCongestion.java             # 엔티티 (festival_congestion, congestion_level 비매핑)
│   ├── *Repository.java                     # DISTINCT ON / ORDER BY..LIMIT 1 네이티브 쿼리로 "최신 1건"만 조회
│   ├── CongestionQueryService.java            # boothId/festivalId 기준 최신 혼잡도 조회
│   └── dto/                                     # BoothCongestionView, FestivalCongestionView
├── global/config/                    # Security, OpenApi(Swagger), Kakao RestClient, Clock
├── global/response/                   # ApiResponse, ErrorCode, SuccessCode 등 공통 응답
├── application/                      # ── 여러 도메인을 엮는 지점(유스케이스 조합)은 여기뿐 ──
│   ├── festival/UserFestivalService.java   # festival+wishlist+roadmap+booth+congestion 조합
│   └── wishlist/UserWishlistService.java    # wishlist + festival 조합
└── api/                               # 컨트롤러 + DTO만 (다른 건 안 둡니다)
    ├── auth/{UserAuthController, dto/*}
    ├── festival/{UserFestivalQueryController, dto/*}   # 서비스 호출만
    └── wishlist/{UserWishlistController, dto/*}          # 서비스 호출만
```

## API 목록

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/auth/kakao/login` | 불필요 | 카카오 인가 코드로 로그인 (최초 로그인 시 자동 회원가입) |
| GET | `/api/festivals` | 선택 | 축제 목록 조회 (`page`, `size`만 지원 — ⚠️ status/name/region 필터는 비회원 서버 에러 원인 파악 전까지 임시 제거). 로그인 상태면 `wishlisted` 표시. `progressStatus`는 파이프라인이 채워둔 DB 컬럼값을 그대로 읽음 |
| GET | `/api/festivals/{festivalId}` | 선택 | 축제 상세 조회. 아래 "상세 조회에 포함되는 정보" 참고 |
| POST | `/api/wishlists/{festivalId}/toggle` | 필요 | 찜 토글 (하트 클릭). 실패 케이스 없이 항상 최종 상태(`wishlisted`)를 돌려줌 |
| GET | `/api/wishlists/me` | 필요 | 내 찜 목록 조회 |

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 상세 조회(`GET /api/festivals/{festivalId}`)에 포함되는 정보

| 필드 | 언제 채워지나 |
|---|---|
| `roadmap` | 관리자/운영자가 로드맵을 저장해뒀으면 항상 (축제 상태 무관). 없으면 `null` |
| `booths` | 부스가 등록돼 있으면 항상 (축제 상태 무관). 없으면 `[]`. 기본 정보(이름/설명/위치)만 |
| `festivalCongestionLevel` | 축제가 **진행중(ONGOING)**일 때만. 그 외엔 `null` |
| `booths[].congestion` | 마찬가지로 **진행중**일 때만 부스별 최신 혼잡도/대기시간(분)이 채워짐. 그 외엔 `null` |

로드맵/부스 정보는 그 자체로는 축제가 언제든 볼 수 있는 "정적 정보"로 취급했고, 혼잡도/대기시간은 "지금 상황"을 나타내는 정보라 진행중일 때만 의미가 있다고 보고 이렇게 나눴습니다.

`festival_congestion`/`booth_congestion` 테이블은 시간에 따라 여러 행이 쌓이는 이력 테이블이라, 매번 **가장 최근 행 하나**만 골라서 보여줍니다 (Postgres `DISTINCT ON`/`ORDER BY ... LIMIT 1` 사용).

⚠️ `congestion_level`, `roadmap_type` 컬럼은 Postgres 네이티브 ENUM 타입이라, 일반 JPA `@Column` 매핑 대신 네이티브 쿼리에서 `::text`로 캐스팅해서 읽습니다 (`BoothCongestionRepository`, `FestivalCongestionRepository`, `FestivalRoadmapRepository` 참고) — 그냥 String으로 매핑하면 `ddl-auto: validate`가 타입 불일치로 실패할 수 있는 알려진 함정이라 피했습니다.

## 실행 전 채워야 하는 값

`cp src/main/resources/application-secret.example.yml src/main/resources/application-secret.yml`

1. **`app.jwt.secret`** — 아무 임의의 긴 문자열
2. **`app.kakao.client-id`** — Kakao Developers > 내 애플리케이션 > 앱 키 > REST API 키
3. **`app.kakao.redirect-uri`** — 카카오 콘솔의 Redirect URI와 정확히 동일해야 함

DB 접속 정보는 `application.yml`의 환경변수로 넘기세요(파이프라인 `config.py`에 쓴 것과 동일한 값):
```bash
export DB_URL=jdbc:postgresql://localhost:5432/실제_DB이름
export DB_USERNAME=실제_유저명
export DB_PASSWORD=실제_비밀번호
```

### 카카오 개발자 콘솔 설정
- 플랫폼: Web 플랫폼 등록 (사이트 도메인 = 프론트엔드 주소)
- 카카오 로그인 활성화: ON
- Redirect URI: 프론트엔드 콜백 주소 등록 (예: `http://localhost:3000/auth/kakao/callback`) — `app.kakao.redirect-uri`와 동일해야 함
- 동의항목: 프로필 정보(닉네임/프로필 사진) 필수 동의 권장. 이메일은 선택 동의로 둬도 코드가 null 처리합니다.
- Client Secret을 켰다면 `app.kakao.client-secret`도 채우세요. 안 켰으면 빈 값(`""`)으로 둡니다.

## 로컬 실행

1. 로컬 Postgres가 떠 있고, 파이프라인이 만든 스키마(위 마이그레이션 포함)가 적용된 상태인지 확인
2. `./gradlew bootRun`
3. Swagger에서 `GET /api/festivals` 먼저 호출해서 파이프라인이 넣은 축제 데이터가 잘 나오는지 확인
4. 카카오 로그인은 프론트엔드(또는 브라우저)에서 카카오 로그인으로 인가 코드를 먼저 받아야
   테스트할 수 있습니다 (백엔드 단독으로는 인가 코드를 못 받습니다).

## `ddl-auto: validate`인 이유

이 서버는 테이블을 만들거나 바꾸지 않습니다 — 스키마는 파이썬 파이프라인이 소유합니다.
`validate`로 해두면, 엔티티 매핑이 실제 테이블 구조와 안 맞을 때(컬럼 이름/타입 불일치 등)
서버 시작 시점에 바로 에러로 알려줘서 잘못된 상태로 조용히 돌아가는 걸 막아줍니다.

## 알려진 한계
- **찜 토글 시 축제 존재 확인은 하지만**(`FestivalQueryService.exists()`), 그 외 축제 자체의
  상태(예: 나중에 게시/비공개 상태 필드가 추가된다면)는 검증하지 않습니다.
- 로드맵/부스/혼잡도는 전부 **읽기 전용**입니다. 등록·수정은 관리자/운영자/알바생 쪽 책임이고,
  이 서버는 만들지 않습니다.
- `booth_congestion`/`festival_congestion`은 "가장 최근 1건"만 보여줍니다 — 시간대별 추이
  그래프 같은 걸 만들려면 이력 전체를 조회하는 API가 추가로 필요합니다.
- 테스트 코드는 이번 범위에서 작성하지 못했습니다.
- 정식 API 계약(요청/응답 필드, 에러 코드 전체 목록)은 `FEATURE_SPEC.md`를 참고하세요.
