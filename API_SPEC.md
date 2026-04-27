# 스마트 냉장고 API 명세서

본 문서는 스마트 냉장고(AI 기반 식품 관리 및 레시피 추천 플랫폼)의 프론트엔드와 백엔드 간 통신을 위한 REST API 명세서입니다.

프론트엔드 목 데이터와의 연동 비용을 줄이기 위해 JSON 필드명은 `snake_case`로 통일합니다.

## 기본 정보

- **운영 Base URL**: `https://food-ai-platform-api.with-momo.com/api/v1`
- **Content-Type**: `application/json`
- **Authorization**: 회원가입, 로그인 외의 모든 API는 JWT 토큰을 포함해야 합니다.
  - `Authorization: Bearer <access_token>`

## 공통 규칙

### 날짜/시간 형식

- 날짜: `YYYY-MM-DD`
- 일시: ISO-8601 local datetime, 예: `2026-04-27T10:30:00`

### 보관 위치

| 값 | 의미 |
| --- | --- |
| `REFRIGERATED` | 냉장 |
| `FROZEN` | 냉동 |
| `ROOM_TEMP` | 실온 |

### 식품 상태

| 값 | 의미 |
| --- | --- |
| `OK` | 유통기한 여유 |
| `WARNING` | 3일 이내 만료 |
| `EXPIRED` | 유통기한 경과 |

### 공통 오류 응답

모든 오류 응답은 아래 형식을 사용합니다.

```json
{
  "timestamp": "2026-04-27T10:30:00",
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다.",
  "details": [
    {
      "field": "expiration_date",
      "reason": "유통기한은 YYYY-MM-DD 형식이어야 합니다."
    }
  ]
}
```

| Status | Code | 사용 예 |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | 잘못된 요청 형식, 잘못된 enum/date |
| `401` | `UNAUTHORIZED` | 로그인 실패, 토큰 누락, 토큰 만료 |
| `403` | `FORBIDDEN` | 다른 사용자의 리소스 접근 |
| `404` | `NOT_FOUND` | 존재하지 않는 식품/레시피 이력 |
| `409` | `CONFLICT` | 중복 이메일 |
| `500` | `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |

## 1. 사용자 인증(Auth)

### 1.1 회원가입

- **POST** `/auth/signup`
- **Authorization**: 불필요

#### Request

```json
{
  "username": "홍길동",
  "email": "user@example.com",
  "password": "password123!"
}
```

#### Response `201 Created`

```json
{
  "message": "회원가입이 완료되었습니다.",
  "user": {
    "user_id": 1,
    "username": "홍길동",
    "email": "user@example.com"
  }
}
```

#### Error

- `400 INVALID_REQUEST`: 필수 값 누락, 이메일 형식 오류, 비밀번호 규칙 불일치
- `409 CONFLICT`: 이미 가입된 이메일

### 1.2 로그인

- **POST** `/auth/login`
- **Authorization**: 불필요

#### Request

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

#### Response `200 OK`

```json
{
  "message": "로그인 성공",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR...",
  "token_type": "Bearer",
  "user": {
    "user_id": 1,
    "username": "홍길동",
    "email": "user@example.com"
  }
}
```

#### Error

- `400 INVALID_REQUEST`: 필수 값 누락, 이메일 형식 오류
- `401 UNAUTHORIZED`: 이메일 또는 비밀번호 불일치

### 1.3 내 정보 조회

- **GET** `/auth/me`

#### Response `200 OK`

```json
{
  "user_id": 1,
  "username": "홍길동",
  "email": "user@example.com"
}
```

## 2. 냉장고 식재료 관리(Fridge Items)

### 2.1 냉장고 현황 요약 조회

- **GET** `/fridge-items/summary`
- **Description**: 메인 좌측 패널의 전체 개수, 보관 위치 통계, 임박/만료 식품 리스트에 사용합니다.

#### Response `200 OK`

```json
{
  "total_count": 9,
  "expiring_soon_count": 2,
  "expired_count": 1,
  "location_stats": {
    "REFRIGERATED": 6,
    "FROZEN": 2,
    "ROOM_TEMP": 1
  },
  "expiring_soon_items": [
    {
      "item_id": 2,
      "name": "대파",
      "d_day": 1,
      "status_text": "D-1"
    },
    {
      "item_id": 3,
      "name": "달걀",
      "d_day": 0,
      "status_text": "오늘 만료"
    }
  ],
  "expired_items": [
    {
      "item_id": 1,
      "name": "바나나",
      "d_day": -2,
      "status_text": "2일 지남"
    }
  ]
}
```

### 2.2 식재료 목록 조회

- **GET** `/fridge-items`
- **Description**: 검색어, 보관 위치, 상태 조건에 맞는 식재료 목록을 유통기한 임박순으로 조회합니다.

#### Query Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `keyword` | String | No | 식품명 검색어 |
| `storage_location` | String | No | `REFRIGERATED`, `FROZEN`, `ROOM_TEMP` |
| `status` | String | No | `OK`, `WARNING`, `EXPIRED` |

#### Response `200 OK`

```json
[
  {
    "item_id": 1,
    "name": "바나나",
    "quantity": "3개",
    "storage_location": "REFRIGERATED",
    "registered_date": "2026-04-01",
    "expiration_date": "2026-04-25",
    "status": "EXPIRED",
    "d_day": -2,
    "status_text": "2일 지남"
  },
  {
    "item_id": 2,
    "name": "대파",
    "quantity": "100g",
    "storage_location": "REFRIGERATED",
    "registered_date": "2026-04-10",
    "expiration_date": "2026-04-28",
    "status": "WARNING",
    "d_day": 1,
    "status_text": "D-1"
  }
]
```

### 2.3 신규 식재료 추가

- **POST** `/fridge-items`

#### Request

```json
{
  "name": "우유",
  "quantity": "500ml",
  "storage_location": "REFRIGERATED",
  "expiration_date": "2026-05-10"
}
```

#### Response `201 Created`

생성된 식재료 리소스를 반환합니다.

```json
{
  "item_id": 10,
  "name": "우유",
  "quantity": "500ml",
  "storage_location": "REFRIGERATED",
  "registered_date": "2026-04-27",
  "expiration_date": "2026-05-10",
  "status": "OK",
  "d_day": 13,
  "status_text": "D-13"
}
```

#### Error

- `400 INVALID_REQUEST`: 필수 값 누락, 잘못된 보관 위치, 잘못된 날짜 형식

### 2.4 식재료 정보 수정

- **PUT** `/fridge-items/{item_id}`

#### Request

```json
{
  "name": "저지방 우유",
  "quantity": "450ml",
  "storage_location": "REFRIGERATED",
  "expiration_date": "2026-05-11"
}
```

#### Response `200 OK`

수정된 식재료 리소스를 반환합니다.

```json
{
  "item_id": 10,
  "name": "저지방 우유",
  "quantity": "450ml",
  "storage_location": "REFRIGERATED",
  "registered_date": "2026-04-27",
  "expiration_date": "2026-05-11",
  "status": "OK",
  "d_day": 14,
  "status_text": "D-14"
}
```

#### Error

- `400 INVALID_REQUEST`: 필수 값 누락, 잘못된 보관 위치, 잘못된 날짜 형식
- `404 NOT_FOUND`: 존재하지 않는 식재료

### 2.5 식재료 삭제

- **DELETE** `/fridge-items/{item_id}`

#### Response `204 No Content`

#### Error

- `404 NOT_FOUND`: 존재하지 않는 식재료

## 3. 사용자 취향(Onboarding Preferences)

### 3.1 사용자 취향 저장

- **PUT** `/users/me/preferences`
- **Description**: 온보딩 설문 결과를 저장합니다. 같은 사용자가 다시 저장하면 기존 값을 덮어씁니다.

#### Request

```json
{
  "favorite_cuisines": ["KOREAN", "JAPANESE"],
  "difficulty_preference": "EASY",
  "quick_meal_preferred": true
}
```

#### Response `200 OK`

```json
{
  "user_id": 1,
  "favorite_cuisines": ["KOREAN", "JAPANESE"],
  "difficulty_preference": "EASY",
  "quick_meal_preferred": true,
  "updated_at": "2026-04-27T10:30:00"
}
```

#### Enum

| 필드 | 값 |
| --- | --- |
| `favorite_cuisines` | `KOREAN`, `JAPANESE`, `WESTERN`, `CHINESE` |
| `difficulty_preference` | `EASY`, `NORMAL`, `HARD` |

### 3.2 사용자 취향 조회

- **GET** `/users/me/preferences`

#### Response `200 OK`

```json
{
  "user_id": 1,
  "favorite_cuisines": ["KOREAN", "JAPANESE"],
  "difficulty_preference": "EASY",
  "quick_meal_preferred": true,
  "updated_at": "2026-04-27T10:30:00"
}
```

## 4. AI 레시피 추천(AI Recipes)

### 4.1 AI 맞춤 레시피 목록 추천

- **GET** `/recipes/recommendations`
- **Description**: 현재 냉장고에 있는 식재료와 사용자 취향을 바탕으로, 임박 식품을 우선 활용하는 레시피를 추천합니다.

#### Response `200 OK`

```json
{
  "available_now": [
    {
      "recipe_id": 101,
      "recipe_name": "대파 달걀 볶음",
      "category": "한식",
      "expiring_ingredients_used": ["대파", "달걀"],
      "all_ingredients": ["대파", "달걀", "양파", "우유"],
      "missing_ingredients": [],
      "instructions": [
        "대파를 송송 썬다.",
        "양파도 채 썬다.",
        "팬에 기름을 두르고 대파와 양파를 볶는다.",
        "양파가 투명해지면 달걀을 풀어 넣고 섞는다.",
        "우유를 조금 넣고 잘 섞어서 스크램블 에그처럼 만든다.",
        "완성된 대파 달걀 볶음을 접시에 담아낸다."
      ]
    }
  ],
  "need_few_ingredients": [
    {
      "recipe_id": 102,
      "recipe_name": "소고기 장조림",
      "category": "한식",
      "expiring_ingredients_used": ["대파"],
      "all_ingredients": ["소고기", "양파", "대파", "간장", "설탕", "다진 마늘"],
      "missing_ingredients": ["간장", "설탕"],
      "instructions": [
        "소고기를 핏물을 빼고 삶는다.",
        "간장, 설탕 등으로 양념장을 만든다.",
        "삶은 소고기와 양념장을 넣고 조린다."
      ]
    }
  ]
}
```

#### Error

- `401 UNAUTHORIZED`: 인증 토큰 누락 또는 만료

## 5. 사용자 레시피 선택 이력(User Recipes)

### 5.1 선택한 레시피 이력 저장

- **POST** `/user-recipes`
- **Description**: 사용자가 추천 모달에서 선택한 레시피를 취향 학습용 이력으로 저장합니다.

#### Request

```json
{
  "recipe_id": 101,
  "recipe_name": "대파 달걀 볶음",
  "category": "한식"
}
```

#### Response `201 Created`

```json
{
  "history_id": 1,
  "recipe_id": 101,
  "recipe_name": "대파 달걀 볶음",
  "category": "한식",
  "selected_at": "2026-04-27T10:30:00"
}
```

#### Error

- `400 INVALID_REQUEST`: 필수 값 누락

### 5.2 나의 레시피 선택 이력 조회

- **GET** `/user-recipes`

#### Query Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `limit` | Number | No | 조회 개수, 기본값 `20` |

#### Response `200 OK`

```json
[
  {
    "history_id": 1,
    "recipe_id": 101,
    "recipe_name": "대파 달걀 볶음",
    "category": "한식",
    "selected_at": "2026-04-27T10:30:00"
  }
]
```
