# 🧊 스마트 냉장고 — AI 기반 식품 관리 & 레시피 추천 플랫폼

> 냉장고 속 식재료를 등록하고, AI가 맞춤 레시피를 추천해주는 웹 서비스

---

## 📌 프로젝트 개요

사용자가 냉장고에 있는 재료들을 플랫폼에 등록하고 관리하면,  
AI가 자동으로 남은 재료를 중심으로 레시피를 맞춤 추천하고,  
유통기한이 얼마 안 남은 식품을 알려주는 **AI 기반 식품 관리 & 레시피 추천 플랫폼**입니다.

단순한 식재료 관리를 넘어 **냉장고 관리 → 레시피 보고 요리하기 → 부족한 재료 장보기**의 전체 과정을 하나의 흐름으로 연결하는 것을 목표로 합니다.

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 회원가입 / 로그인 | 이메일 + 비밀번호 기반 JWT 인증 |
| 🥗 식재료 등록 & 관리 | 식품명, 수량, 보관 위치, 유통기한 CRUD |
| 🔍 검색 & 필터 | 식품명 키워드 검색, 냉장/냉동/실온 필터링 |
| ⏰ 유통기한 관리 | 만료 여부에 따라 상태 자동 분류 (OK / WARNING / EXPIRED) |
| ✨ AI 레시피 추천 | Gemini API 기반, 보유 재료로 만들 수 있는 레시피 + 재료 조금만 추가하면 되는 레시피 추천 |
| 👍 레시피 선택 이력 | 선택한 레시피를 서버에 저장하여 취향 개인화에 활용 |
| ⚙️ 취향 설정 | 선호 음식 종류(한식/일식/양식/중식), 조리 난이도, 간편식 여부 온보딩 설정 |

---

## 🖥️ 화면 구성

### 메인 화면
- 냉장고 현황 요약 — 전체 식품 수 / 유통기한 임박 수 / 만료 수
- 보관 위치별 현황 바 — 냉장 / 냉동 / 실온 비율
- 임박/만료 식품 카드 목록
- 식재료 카드 그리드 (상태별 색상 구분)

### 식품 추가 / 수정 화면
- 식품명, 수량, 보관 위치(냉장/냉동/실온), 유통기한 입력

### AI 레시피 추천 화면
- **지금 바로 만들 수 있어요** — 현재 보유 재료만으로 완성 가능한 레시피
- **재료 조금만 사면 만들 수 있어요** — 1~2가지만 추가 구매하면 되는 레시피
- 각 레시피: 전체 재료 태그, 부족한 재료 태그, 조리법 토글

---

## 🗄️ 데이터베이스 설계 (ERD)

### 핵심 엔티티

#### 👤 USER_ACCOUNT (사용자 계정)
> JWT 인증 기반 로그인을 위한 계정 정보

| 컬럼 | 타입 | 설명 |
|------|------|------|
| user_id | BIGINT (PK) | 고유 식별키 |
| username | VARCHAR | 사용자명 |
| email | VARCHAR (UNIQUE) | 로그인용 이메일 |
| password | VARCHAR | bcrypt 암호화 비밀번호 |

---

#### 🧊 FRIDGE_ITEM (냉장고 식재료)
> 사용자별 보유 식재료 및 유통기한 트래킹

| 컬럼 | 타입 | 설명 |
|------|------|------|
| item_id | BIGINT (PK) | 고유 식별키 |
| user_id | BIGINT (FK) | 소유 사용자 |
| name | VARCHAR | 식품명 |
| quantity | VARCHAR | 수량 (예: 3개, 500ml) |
| storage_location | ENUM | 보관 위치 (REFRIGERATED / FROZEN / ROOM_TEMP) |
| registered_date | DATE | 등록일 |
| expiration_date | DATE | 유통기한 |

---

#### ⚙️ USER_PREFERENCE (사용자 취향 설정)
> 레시피 추천 개인화를 위한 취향 데이터

| 컬럼 | 타입 | 설명 |
|------|------|------|
| preference_id | BIGINT (PK) | 고유 식별키 |
| user_id | BIGINT (FK) | 소유 사용자 |
| favorite_cuisines | VARCHAR | 선호 음식 종류 (KOREAN / JAPANESE / WESTERN / CHINESE) |
| difficulty_preference | ENUM | 조리 난이도 (EASY / NORMAL / HARD) |
| quick_meal_preferred | BOOLEAN | 간편식 선호 여부 |

---

#### 🍽️ USER_RECIPE_HISTORY (레시피 선택 이력)
> 사용자가 선택한 레시피 이력 — 취향 개인화에 활용

| 컬럼 | 타입 | 설명 |
|------|------|------|
| history_id | BIGINT (PK) | 고유 식별키 |
| user_id | BIGINT (FK) | 소유 사용자 |
| recipe_id | BIGINT | 외부 레시피 식별자 |
| recipe_name | VARCHAR | 레시피 이름 |
| category | VARCHAR | 카테고리 (한식 / 일식 등) |
| selected_at | DATETIME | 선택 시각 |

---

### 엔티티 관계
USER_ACCOUNT ──< FRIDGE_ITEM          (1:N)
USER_ACCOUNT ──< USER_PREFERENCE      (1:1)
USER_ACCOUNT ──< USER_RECIPE_HISTORY  (1:N)
---

## ⚙️ 시스템 아키텍처
[브라우저 — Vanilla JS]
↕ HTTP / JWT
[Spring Boot REST API]
├─ WebMvcAuthConfig (Interceptor 기반 JWT 인증)
├─ Controller → Service → Repository
└─ Gemini API 연동 (AI 레시피 추천)
[MySQL / MariaDB]
### 요청 흐름
1. **요청 발생** — 사용자 클릭 → `authFetch()` → Authorization: Bearer {JWT}
2. **인증 검증** — `AuthInterceptor`가 JWT 파싱 → `AuthenticationContext`에 유저 정보 저장
3. **로직 처리** — Controller → Service → Repository → DB 조회/저장
4. **AI 추천** — `GeminiRecipeRecommendationEngine`이 냉장고 재료 + 취향 기반 Gemini API 호출
5. **응답 반환** — DTO → JSON → 프론트 렌더링

---

## 🧑‍💻 기술 스택

### 프론트엔드
- HTML5 / CSS3 / Vanilla JavaScript
- 파일 분리 구조: `data.js` / `render.js` / `modal.js` / `main.js`
- JWT는 `sessionStorage`에 저장, `authFetch()` 공통 함수로 인증 헤더 자동 주입

### 백엔드
- Java 17 / Spring Boot
- Spring MVC (Interceptor 기반 JWT 인증, Spring Security 미사용)
- Gemini API (AI 레시피 추천)
- Springdoc OpenAPI (Swagger UI)

### 데이터베이스
- MySQL / MariaDB
- Spring Data JPA

### 인프라
- Docker / GitHub Actions (CI/CD)
- 배포 주소: `https://food-ai-platform-api.with-momo.com/api/v1`

---

## 📁 프로젝트 구조
food-ai-platform/
├── food-ai-platform-web/          # 프론트엔드
│   ├── index.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── data.js                # 전역 상수 & location 변환 유틸
│       ├── render.js              # DOM 렌더링 (식품 목록 / 사이드바 / 레시피)
│       ├── modal.js               # 모달 & CRUD & 로그인 / 로그아웃
│       └── main.js                # DOMContentLoaded 초기화 & 이벤트 등록
│
└── food-ai-platform-server/       # 백엔드
└── src/main/java/com/example/foodaiplatformserver/
├── auth/                  # 회원가입 / 로그인 / JWT
│   ├── controller/        # AuthController
│   ├── security/          # JwtTokenProvider, AuthInterceptor
│   └── service/           # AuthService
├── fridgeitem/            # 냉장고 식재료 CRUD
│   ├── controller/        # FridgeItemController
│   └── service/           # FridgeItemService, FridgeItemStatusCalculator
├── recipe/                # AI 레시피 추천
│   ├── controller/        # RecipeRecommendationController
│   └── service/           # GeminiRecipeRecommendationEngine
├── user/                  # 사용자 취향 설정
│   ├── controller/        # UserPreferenceController
│   └── service/           # UserPreferenceService
├── userrecipe/            # 레시피 선택 이력
│   ├── controller/        # UserRecipeController
│   └── service/           # UserRecipeService
└── common/                # 공통 예외 / 응답 / 유틸
---

## 🌐 REST API 목록

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/auth/signup` | - | 회원가입 |
| POST | `/api/v1/auth/login` | - | 로그인 (JWT 발급) |
| GET | `/api/v1/fridge-items` | ✅ | 식재료 목록 조회 (검색/필터) |
| POST | `/api/v1/fridge-items` | ✅ | 식재료 추가 |
| PUT | `/api/v1/fridge-items/{id}` | ✅ | 식재료 수정 |
| DELETE | `/api/v1/fridge-items/{id}` | ✅ | 식재료 삭제 |
| GET | `/api/v1/fridge-items/summary` | ✅ | 냉장고 현황 요약 |
| GET | `/api/v1/recipes/recommendations` | ✅ | AI 레시피 추천 |
| POST | `/api/v1/user-recipes` | ✅ | 레시피 선택 이력 저장 |
| GET | `/api/v1/users/me/preferences` | ✅ | 취향 설정 조회 |
| PUT | `/api/v1/users/me/preferences` | ✅ | 취향 설정 저장 |

---

## 🚀 실행 방법

### 프론트엔드

```bash
# 1. 저장소 클론
git clone https://github.com/jono03/food-ai-platform.git
cd food-ai-platform

# 2. VSCode에서 열고 Live Server 확장으로 index.html 실행
# 또는 단순히 index.html을 브라우저에서 열기
```

### 백엔드

```bash
cd food-ai-platform-server

# 환경변수 설정 (application.properties 또는 환경변수)
# DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, GEMINI_API_KEY

./gradlew bootRun
```

> Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 👤 사용자 시나리오

1. **온보딩** — 최초 접속 시 선호 음식 종류 & 조리 난이도 설정
2. **로그인** — 이메일 + 비밀번호로 로그인 → JWT 발급
3. **식재료 등록** — "식품 추가" 버튼 → 정보 입력 → 냉장고 목록 자동 반영
4. **유통기한 확인** — 접속 시 임박/만료 식품 색상으로 강조 표시
5. **AI 레시피 추천** — "AI 레시피 추천받기" 버튼 → 현재 재료 기반 레시피 확인 → 선택
6. **취향 누적** — 선택한 레시피가 이력에 저장 → 이후 추천 개인화

---

## 🎯 프로젝트 목표

- 식재료 낭비 줄이기 (유통기한 관리)
- 음식물 쓰레기 감축
- 냉장고 관리 효율 향상
- 개인 맞춤형 레시피 추천으로 요리 실행 돕기
