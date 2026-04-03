# 🧊 냉장고 AI - 식재료 관리 & 레시피 추천 플랫폼

냉장고 속 재료를 등록하면 **유통기한 임박 재료를 우선**으로 AI가 레시피를 추천하는 1인 가구 맞춤 서비스입니다.

## 🎯 프로젝트 소개

**문제**: 1인 가구의 냉장고 속 재료가 유통기한을 넘겨 버려지는 경우가 많습니다.  
**해결**: 냉장고 재료를 등록 → AI가 임박 재료 우선으로 레시피 추천 → 부족 재료만 장보기 리스트 생성.

### 핵심 가치
- 🥬 **식재료 낭비 0** - 유통기한 임박 재료 자동 우선 추천
- ⏱️ **요리 결정 5초** - 현재 재료 기반 즉시 레시피 생성
- 💰 **장보기 절약** - 이미 있는 재료 중복 구매 방지
- 🌱 **환경 기여** - 음식물 쓰레기 감소로 온실가스 저감

## 🚀 주요 기능

| 기능 | 설명 |
|------|------|
| **식재료 관리** | 이름, 수량, 보관위치, 유통기한 등록/수정/삭제 |
| **유통기한 알림** | 빨강(임박)→노랑(주의)→초록(여유) 색상 구분 |
| **AI 레시피 추천** | ① 현재 재료만으로 가능한 요리<br>② 1~2개 재료 추가 요리 |
| **부족 재료 안내** | 추천 레시피별 필요한 재료 자동 계산 |
| **취향 학습** | 선택한 레시피 기록 → 개인화 추천 |
| **장보기 리스트** | 부족 재료만 자동 생성 |

## 🛠️ 기술 스택

### Frontend
- React 18.x + Vite
- TypeScript/ES6+
- Axios + React Hooks/Context API
- Styled-components/CSS Modules

### Backend
- Spring Boot 3.x + Java 17
- Spring Data JPA + MySQL
- Spring Security + JWT
- Spring WebClient (AI 통신)
- Swagger API 문서화

### Infrastructure
- Docker (배포)
- Git Flow (브랜치 전략)
- Figma (디자인 시스템)

## 🗄️ 데이터베이스 설계

```mermaid
erDiagram
    USER {
        int user_id PK
        string username
        string email
        string password
    }
    FRIDGE_ITEM {
        int item_id PK
        int user_id FK
        string storage_location
        float quantity
        date registered_date
        date expiration_date
    }
    USER_RECIPE {
        int recipe_id PK
        int user_id FK
        string recipe_name
        string category
    }
    USER ||--o{ FRIDGE_ITEM : "소유"
    USER ||--o{ USER_RECIPE : "선호"
