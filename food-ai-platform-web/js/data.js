// ============================================================
//  data.js  —  앱 전체에서 공유하는 데이터와 상수
//  엔티티: USER / FRIDGE_ITEM / USER_RECIPE
// ============================================================

// ── API BASE URL ─────────────────────────────────────────────
const BASE_URL = "https://food-ai-platform-api.with-momo.com/api/v1";

// ── storage_location 변환 ────────────────────────────────────
// 프론트 표시값 → API enum
const LOCATION_TO_API = {
  "🧊 냉장" : "REFRIGERATED",
  "❄️ 냉동" : "FROZEN",
  "🏠 실온" : "ROOM_TEMP",
};

// API enum → 프론트 표시값
const LOCATION_FROM_API = {
  "REFRIGERATED" : "🧊 냉장",
  "FROZEN"       : "❄️ 냉동",
  "ROOM_TEMP"    : "🏠 실온",
};

/** 프론트 표시값 → API enum 변환 */
function toApiLocation(frontLabel) {
  return LOCATION_TO_API[frontLabel] ?? frontLabel;
}

/** API enum → 프론트 표시값 변환 */
function fromApiLocation(apiEnum) {
  return LOCATION_FROM_API[apiEnum] ?? apiEnum;
}

// ── USER (현재 로그인한 사용자) ──────────────────────────────
// 실제 서비스에서는 서버에서 받아올 값
let currentUser = null;
/*
  currentUser 구조:
  {
    user_id  : 1,
    username : "홍길동",
    email    : "hong@email.com",
    password : "(서버에서 관리, 프론트에선 보관 안 함)"
  }
*/

// ── FRIDGE_ITEM (냉장고 식재료 목록) ────────────────────────
// 실제 서비스에서는 GET /api/fridge-items 로 받아올 값
let fridgeItems = [
  {
    item_id          : 1,
    user_id          : 1,
    name             : "바나나",       // 식품명 (UI 편의용)
    quantity         : "3개",
    storage_location : "냉장",
    registered_date  : "2026-04-01",
    expiration_date  : "2026-07-18",
  },
  {
    item_id          : 2,
    user_id          : 1,
    name             : "달걀",
    quantity         : "3개",
    storage_location : "냉장",
    registered_date  : "2026-04-01",
    expiration_date  : "2026-07-16",
  },
  {
    item_id          : 3,
    user_id          : 1,
    name             : "대파",
    quantity         : "100g",
    storage_location : "냉장",
    registered_date  : "2026-04-10",
    expiration_date  : "2026-07-15",
  },
  {
    item_id          : 4,
    user_id          : 1,
    name             : "배추",
    quantity         : "300g",
    storage_location : "냉장",
    registered_date  : "2026-04-10",
    expiration_date  : "2026-07-11",
  },
  {
    item_id          : 5,
    user_id          : 1,
    name             : "양파",
    quantity         : "300g",
    storage_location : "냉장",
    registered_date  : "2026-04-05",
    expiration_date  : "2026-07-10",
  },
  {
    item_id          : 6,
    user_id          : 1,
    name             : "소고기",
    quantity         : "200g",
    storage_location : "냉동",
    registered_date  : "2026-04-01",
    expiration_date  : "2026-07-08",
  },
  {
    item_id          : 7,
    user_id          : 1,
    name             : "돼지고기",
    quantity         : "300g",
    storage_location : "냉동",
    registered_date  : "2026-04-01",
    expiration_date  : "2026-07-07",
  },
  {
    item_id          : 8,
    user_id          : 1,
    name             : "우유",
    quantity         : "500ml",
    storage_location : "냉장",
    registered_date  : "2026-04-20",
    expiration_date  : "2026-07-06",
  },
  {
    item_id          : 9,
    user_id          : 1,
    name             : "스팸",
    quantity         : "3개",
    storage_location : "실온",
    registered_date  : "2026-04-01",
    expiration_date  : "2026-12-31",
  },
];

let nextItemId = 10; // 새 아이템 추가 시 사용할 임시 ID

// ── USER_RECIPE ───────────────────────────────────────────────
// POST /user-recipes API 연결로 서버에 저장 (로컬 배열 불필요)

// ── RECIPE 목록 ───────────────────────────────────────────────
// GET /recipes/recommendations API 응답으로 받아오므로 로컬 풀 제거

// ── 온보딩 설문 데이터 ───────────────────────────────────────
const ONBOARD_STEPS = [
  {
    question : "좋아하는 음식 종류는?",
    options  : ["🍚 한식", "🍜 일식", "🍝 양식", "🌮 중식"],
    multi    : true,
  },
  {
    question : "선호하는 조리 난이도는?",
    options  : ["😊 쉬움", "💪 보통", "🔥 어려워도 OK", "⚡ 간편식 선호"],
    multi    : false,
  },
];