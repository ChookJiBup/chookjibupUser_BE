# chookjibupUser_BE 기능 명세표

이 문서는 `chookjibupUser_BE`(사용자 백엔드)에 실제로 구현된 기능의 API 계약을 정리한 것입니다.
설계/구조 설명은 `README.md`를 참고하세요.

## 1. 기능 목록 요약

| No | 기능 분류 | 기능명 | Method | Endpoint | 인증 |
|---|---|---|---|---|---|
| 1 | 인증 | 카카오 로그인(자동 회원가입) | POST | `/api/auth/kakao/login` | 불필요 |
| 2 | 축제 | 축제 목록 조회 | GET | `/api/festivals` | 선택 |
| 3 | 축제 | 축제 상세 조회 (+로드맵/부스/혼잡도) | GET | `/api/festivals/{festivalId}` | 선택 |
| 4 | 찜 | 찜 토글 (하트 클릭) | POST | `/api/wishlists/{festivalId}/toggle` | 필요 |
| 5 | 찜 | 내 찜 목록 조회 | GET | `/api/wishlists/me` | 필요 |

인증 컬럼 표기: **불필요**(로그인 없이 호출), **선택**(비회원도 되지만 로그인하면 결과가 달라짐), **필요**(JWT 없으면 401).

---

## 2. 공통 응답 규격

모든 API는 아래 형태로 감싸서 응답합니다.

```json
{
  "code": 20000,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```

- 성공/실패 모두 HTTP 상태코드 + 이 JSON 바디를 함께 내려줍니다.
- 인증이 필요한 API는 요청 헤더에 `Authorization: Bearer {accessToken}`을 실어야 합니다.

---

## 3. 기능별 상세 명세

### 3-1. 카카오 로그인

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /api/auth/kakao/login` |
| 인증 | 불필요 |
| 설명 | 프론트가 카카오 로그인으로 받은 인가 코드를 넘기면, 서버가 카카오와 직접 통신해서 토큰 교환 → 사용자 정보 조회 → 계정 조회/생성 → 자체 JWT 발급까지 처리한다. 가입 이력이 없으면 자동 회원가입된다. |
| 담당 서비스 | `KakaoLoginService` |

**요청 (`KakaoLoginRequest`)**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `code` | String | ✅ | 카카오 인가 코드 |
| `redirectUri` | String | ❌ | 인가 코드 발급 시 쓴 redirect_uri. 생략하면 서버 기본값(`app.kakao.redirect-uri`) 사용 |

**응답 데이터 (`UserLoginResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `accessToken` | String | 이후 요청에 실을 JWT |
| `accessTokenExpiresInSeconds` | long | 토큰 만료까지 남은 초 |
| `newUser` | boolean | 이번 로그인으로 신규 가입됐는지 |
| `nickname` | String | 카카오 프로필 닉네임 (동의 없으면 "카카오사용자") |
| `email` | String \| null | 카카오 계정 이메일 (선택 동의 항목, 동의 안 하면 null) |
| `profileImageUrl` | String \| null | 카카오 프로필 사진 URL |

**실패 케이스**

| 상황 | ErrorCode |
|---|---|
| `code` 값이 비어있음 | `AUTH_KAKAO_CODE_REQUIRED` (400) |
| 카카오 토큰 교환/사용자 조회 실패 (잘못된 코드, 만료된 코드, redirect_uri 불일치 등) | `AUTH_KAKAO_LOGIN_FAILED` (401) |

---

### 3-2. 축제 목록 조회

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /api/festivals` |
| 인증 | 선택 — 비회원도 호출 가능. 로그인 상태면 항목마다 `wishlisted` 채워짐 |
| 담당 서비스 | `UserFestivalService.getFestivals()` (festival + wishlist 도메인 조합) |

**요청 파라미터**

> ⚠️ [임시] `status`/`name`/`region` 필터는 비회원 조회 시 서버 에러가 발생해서 원인 파악
> 전까지 잠시 제거했습니다. 지금은 페이지네이션만 지원합니다.

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `page` | int | ❌ | 0 | 0부터 시작 |
| `size` | int | ❌ | 20 | 최대 100 |

**응답 데이터 (`UserFestivalPageResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `items[]` | `UserFestivalResponse[]` | 아래 표 참고 |
| `page` / `size` / `totalElements` / `totalPages` | int / int / long / int | 페이지 메타정보 |

**`items[]` 항목 (`UserFestivalResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `festivalId` | Long | |
| `name` | String | 축제명 |
| `eventPlace` | String \| null | 개최 장소명 |
| `address` | String \| null | 도로명주소 |
| `startDate` / `endDate` | LocalDate \| null | 개최기간 |
| `phoneNumber` / `homepageUrl` | String \| null | |
| `progressStatus` | `UPCOMING`\|`ONGOING`\|`COMPLETED`\|null | **파이썬 파이프라인이 새벽 6시 배치로 채워둔 `festivals.progress_status` 컬럼값을 그대로 읽은 것.** 이 서버가 직접 계산하지 않는다. 파이프라인이 아직 값을 안 채웠거나 `start_date`/`end_date`가 없어서 판단 불가능한 축제는 null |
| `wishlisted` | boolean | 비회원 조회면 항상 false |

**실패 케이스**: `page < 0` 또는 `size`가 1~100 범위를 벗어나면 `INVALID_REQUEST` (400).

---

### 3-3. 축제 상세 조회

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /api/festivals/{festivalId}` |
| 인증 | 선택 |
| 담당 서비스 | `UserFestivalService.getFestivalDetail()` (festival + wishlist + roadmap + booth + congestion 5개 도메인 조합) |

**응답 데이터 (`UserFestivalDetailResponse`)**

| 필드 | 타입 | 채워지는 조건 |
|---|---|---|
| `festivalId`, `name`, `eventPlace`, `address`, `startDate`, `endDate`, `content`, `phoneNumber`, `homepageUrl`, `progressStatus` | - | 항상 (축제 기본 정보) |
| `wishlisted` | boolean | 항상 (비회원이면 false) |
| `roadmap` | `RoadmapResponse` \| null | **로드맵이 저장돼 있으면 항상.** 축제 상태 무관. 없으면 `null` |
| `booths[]` | `BoothResponse[]` | **부스가 등록돼 있으면 항상.** 축제 상태 무관. 없으면 `[]` |
| `festivalCongestionLevel` | String \| null | 축제가 **진행중(ONGOING)**이고 혼잡도 기록이 있을 때만. 그 외엔 `null` |
| `booths[].congestion` | `BoothCongestionResponse` \| null | 축제가 **진행중**이고 해당 부스에 혼잡도 기록이 있을 때만. 그 외엔 `null` |

**`roadmap` 하위 구조 (`RoadmapResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `roadmapType` | String | `uploaded_image`(팜플렛 업로드) \| `icon_builder`(아이콘 직접 배치) |
| `baseImageUrl` | String \| null | 업로드 이미지 또는 배경 트레이싱용 이미지 |
| `canvasWidth` / `canvasHeight` | Integer \| null | `icon_builder`일 때 캔버스 크기(px) |
| `icons[]` | `RoadmapIconResponse[]` | 캔버스에 배치된 아이콘 목록 |

**`icons[]` 항목 (`RoadmapIconResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `placementId` | Long | |
| `iconCode` / `iconName` / `iconImageUrl` | String | 아이콘 종류 (부스/화장실/주차장 등) |
| `relatedBoothId` | Long \| null | 부스 아이콘일 때만 값 있음. 화장실 등은 null |
| `positionX` / `positionY` | BigDecimal | 캔버스 내 좌표 |
| `rotationDeg` | BigDecimal | 회전 각도 |
| `label` | String \| null | 커스텀 라벨 (예: "화장실 A") |

**`booths[]` 항목 (`BoothResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `boothId` / `boothName` | Long / String | |
| `boothContent` / `boothLocation` | String \| null | |
| `congestion` | `BoothCongestionResponse` \| null | 진행중이 아니거나 혼잡도 기록이 없으면 `null` |

**`congestion` 하위 구조 (`BoothCongestionResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `congestionLevel` | String | `crowded`(혼잡) \| `normal`(보통) \| `comfortable`(여유) |
| `waitMinutes` | Integer \| null | 대기시간(분) |
| `updatedAt` | OffsetDateTime | 마지막으로 갱신된 시각 (여러 기록 중 최신 1건) |

**실패 케이스**

| 상황 | ErrorCode |
|---|---|
| `festivalId`에 해당하는 축제가 없음 | `FESTIVAL_NOT_FOUND` (404) |
| 로드맵/부스/혼잡도 기록이 없음 | **에러 아님** — 각각 `null`/`[]`로 정상 응답 |

---

### 3-4. 찜 토글 (하트 클릭)

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /api/wishlists/{festivalId}/toggle` |
| 인증 | 필요 |
| 설명 | 찜 안 한 상태에서 호출하면 찜하고, 이미 찜한 상태에서 호출하면 취소한다. **두 경우 모두 정상 동작이라 실패 응답이 없다.** 동시에 두 번 클릭돼서 DB 유니크 제약(`user_id`+`festival_id`)에 걸리는 경우도 "찜됨"으로 정상 처리한다. |
| 담당 서비스 | `UserWishlistService.toggle()` (wishlist + festival 도메인 조합) |

**응답 데이터 (`WishlistToggleResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `festivalId` | Long | |
| `wishlisted` | boolean | 토글 후 최종 상태 (true=찜됨, false=찜 취소됨) |

**실패 케이스**

| 상황 | ErrorCode |
|---|---|
| JWT 없음/만료/위조 | `UNAUTHORIZED`(401) / `AUTH_TOKEN_EXPIRED`(401) / `AUTH_TOKEN_INVALID`(401) |
| `festivalId`에 해당하는 축제가 없음 | `FESTIVAL_NOT_FOUND` (404) |

---

### 3-5. 내 찜 목록 조회

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /api/wishlists/me` |
| 인증 | 필요 |
| 담당 서비스 | `UserWishlistService.getMyWishlist()` (wishlist + festival 도메인 조합) |

**요청 파라미터**

| 파라미터 | 타입 | 필수 | 기본값 |
|---|---|---|---|
| `page` | int | ❌ | 0 |
| `size` | int | ❌ | 20 (최대 100) |

**응답 데이터 (`MyWishlistPageResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `items[]` | `MyWishlistFestivalResponse[]` | 찜한 순서 최신순 |
| `page` / `size` / `totalElements` / `totalPages` | - | 페이지 메타정보 |

**`items[]` 항목 (`MyWishlistFestivalResponse`)**

| 필드 | 타입 | 설명 |
|---|---|---|
| `festivalId` / `name` / `eventPlace` / `address` / `startDate` / `endDate` | - | 축제 기본 정보 |
| `wishlistedAt` | OffsetDateTime | 찜한 시각 |

찜한 뒤에 파이프라인 쪽에서 해당 축제 데이터가 삭제된 경우, 그 항목은 목록에서 조용히 제외됩니다(에러 아님).

---

## 4. 에러 코드 전체 목록

| 코드 | HTTP 상태 | 이름 | 메시지 | 발생 위치 |
|---|---|---|---|---|
| 40000 | 400 | `BAD_REQUEST` | 잘못된 요청입니다. | (현재 미사용, 확장 대비) |
| 40001 | 400 | `INVALID_REQUEST` | 요청 값이 올바르지 않습니다. | 페이지/사이즈 파라미터 범위 벗어남, 요청 바디 `@Valid` 검증 실패 |
| 40002 | 400 | `AUTH_KAKAO_CODE_REQUIRED` | 카카오 인가 코드가 필요합니다. | 카카오 로그인 |
| 40100 | 401 | `UNAUTHORIZED` | 인증이 필요합니다. | JWT 없이 인증 필요 API 호출 |
| 40103 | 401 | `AUTH_TOKEN_INVALID` | 유효하지 않은 인증 토큰입니다. | JWT 서명 불일치/파싱 실패 |
| 40104 | 401 | `AUTH_TOKEN_EXPIRED` | 만료된 인증 토큰입니다. | JWT 만료 |
| 40105 | 401 | `AUTH_KAKAO_LOGIN_FAILED` | 카카오 로그인에 실패했습니다. | 카카오 토큰 교환/사용자 조회 실패 |
| 40300 | 403 | `FORBIDDEN` | 권한이 없습니다. | (현재 미사용, 확장 대비) |
| 40401 | 404 | `USER_NOT_FOUND` | 사용자를 찾을 수 없습니다. | (현재 미사용, 확장 대비) |
| 40402 | 404 | `FESTIVAL_NOT_FOUND` | 축제를 찾을 수 없습니다. | 축제 상세 조회, 찜 토글 |
| 50000 | 500 | `INTERNAL_SERVER_ERROR` | 서버 에러가 발생하였습니다. | 처리되지 않은 예외 |

## 5. 성공 코드 전체 목록

| 코드 | 이름 | 메시지 | 사용 API |
|---|---|---|---|
| 20000 | `OK` | 요청이 성공적으로 처리되었습니다. | (범용) |
| 21000 | `USER_KAKAO_LOGIN_SUCCESS` | 카카오 로그인에 성공했습니다. | 카카오 로그인 |
| 22000 | `FESTIVAL_LIST_READ_SUCCESS` | 축제 목록 조회가 완료되었습니다. | 축제 목록/상세 조회 |
| 23000 | `WISHLIST_TOGGLE_SUCCESS` | 찜 상태가 변경되었습니다. | 찜 토글 |
| 23002 | `WISHLIST_READ_SUCCESS` | 찜한 축제 목록 조회가 완료되었습니다. | 내 찜 목록 조회 |

## 6. 기능 ↔ 도메인 ↔ 테이블 매핑

| 기능 | 관련 도메인 | 관련 테이블 | 쓰기 가능 여부 |
|---|---|---|---|
| 카카오 로그인 | `user`, `auth` | `users` | 이 서버가 씀 (INSERT/UPDATE) |
| 축제 목록/상세 | `festival` | `festivals` | 읽기 전용 |
| 로드맵 | `roadmap` | `festival_roadmap`, `roadmap_icon_placement`, `roadmap_icon_type` | 읽기 전용 |
| 부스 정보 | `booth` | `booth_info` | 읽기 전용 |
| 혼잡도 | `congestion` | `booth_congestion`, `festival_congestion` | 읽기 전용 |
| 찜 | `wishlist` | `festival_wishlist` | 이 서버가 씀 (INSERT/DELETE) |

`festivals`, `festival_roadmap`, `roadmap_icon_placement`, `roadmap_icon_type`, `booth_info`,
`booth_congestion`, `festival_congestion`은 전부 관리자/운영자/알바생 쪽(또는 파이썬
파이프라인)이 채우는 테이블이고, 이 서버는 조회만 합니다.
