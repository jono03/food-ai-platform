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

// ── USER_RECIPE (사용자가 선택한 레시피 이력) ────────────────
// 실제 서비스에서는 POST /api/user-recipes 로 저장
let userRecipes = [];
/*
  userRecipes 배열 원소 구조:
  {
    recipe_id   : 1,
    user_id     : 1,
    recipe_name : "스팸 볶음밥",
    category    : "한식"
  }
*/
let nextRecipeId = 1;

// ── RECIPE 목록 (AI가 추천할 레시피 풀) ─────────────────────
// 실제 서비스에서는 AI API 응답으로 받아올 값
const RECIPE_POOL = [
  {
    recipe_name : "스팸 볶음밥",
    category    : "한식",
    icon        : "🍳",
    need        : ["스팸"],
    extra       : [],
    steps       : [
      "스팸을 깍둑썰기한다.",
      "팬에 기름을 두르고 스팸을 볶는다.",
      "밥을 넣고 함께 볶다가 간장으로 간을 맞춘다.",
    ],
  },
  {
    recipe_name : "스팸 김치찌개",
    category    : "한식",
    icon        : "🍲",
    need        : ["스팸"],
    extra       : [],
    steps       : [
      "냄비에 김치와 스팸을 넣는다.",
      "물을 붓고 끓인다.",
      "고춧가루, 간장으로 간을 맞추고 파를 얹는다.",
    ],
  },
  {
    recipe_name : "대파 달걀볶음",
    category    : "한식",
    icon        : "🥚",
    need        : ["대파", "달걀"],
    extra       : [],
    steps       : [
      "달걀을 풀어 스크램블한다.",
      "대파를 송송 썬다.",
      "팬에 대파를 넣고 달걀과 함께 볶고 소금으로 간한다.",
    ],
  },
  {
    recipe_name : "배추 된장국",
    category    : "한식",
    icon        : "🥣",
    need        : ["배추"],
    extra       : [],
    steps       : [
      "배추를 먹기 좋게 썬다.",
      "냄비에 물을 끓이고 된장을 푼다.",
      "배추를 넣고 5분 끓인 뒤 파를 얹는다.",
    ],
  },
  {
    recipe_name : "스팸 우동",
    category    : "일식",
    icon        : "🍜",
    need        : ["스팸", "대파"],
    extra       : ["우동면"],
    steps       : [
      "육수를 끓인다.",
      "우동면과 스팸을 넣는다.",
      "대파와 간장으로 마무리한다.",
    ],
  },
  {
    recipe_name : "스팸 롤",
    category    : "일식",
    icon        : "🍱",
    need        : ["스팸"],
    extra       : ["김밥용 김"],
    steps       : [
      "스팸을 구워 준비한다.",
      "밥을 펴고 스팸을 올린다.",
      "김으로 감싸 먹기 좋게 썬다.",
    ],
  },
  {
    recipe_name : "소고기 볶음",
    category    : "한식",
    icon        : "🥩",
    need        : ["소고기", "양파"],
    extra       : ["간장", "설탕"],
    steps       : [
      "소고기를 간장·설탕·참기름에 재운다.",
      "양파를 채 썬다.",
      "팬에 소고기와 양파를 함께 볶는다.",
    ],
  },
  {
    recipe_name : "돼지고기 두루치기",
    category    : "한식",
    icon        : "🌶️",
    need        : ["돼지고기", "양파", "대파"],
    extra       : ["고추장"],
    steps       : [
      "돼지고기를 먹기 좋게 썬다.",
      "고추장 양념에 버무린다.",
      "양파·대파와 함께 팬에 볶는다.",
    ],
  },
];

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