// ============================================================
//  render.js  —  DOM에 데이터를 그리는 함수들
// ============================================================

// ── 현재 필터 & 검색 상태 ────────────────────────────────────
let currentFilter = "all";

// ── 날짜 유틸 ────────────────────────────────────────────────

/** 오늘 기준 남은 일수 (음수면 만료) */
function getDaysLeft(expiration_date) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const exp = new Date(expiration_date);
  exp.setHours(0, 0, 0, 0);
  return Math.ceil((exp - today) / 86400000);
}

/** 남은 일수 → "expired" | "expiring" | "ok" */
function getStatus(days) {
  if (days < 0) return "expired";
  if (days <= 3) return "expiring";
  return "ok";
}

/** 남은 일수 → 표시 텍스트 */
function formatDays(days) {
  if (days < 0)  return `${Math.abs(days)}일 지남`;
  if (days === 0) return "오늘 만료";
  return `D-${days}`;
}

/** storage_location 에서 한글만 추출 (이모지 제거) */
function locLabel(loc) {
  return loc.replace(/[^\uAC00-\uD7A3]/g, "");
}

/** XSS 방지 — innerHTML 삽입 전 HTML 특수문자 이스케이프 */
function escapeHtml(str) {
  return String(str ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// ── 식품 목록 렌더링 ─────────────────────────────────────────

/** GET /fridge-items — 검색/필터 조건으로 목록 조회 후 렌더링 */
async function renderItems() {
  // fix #76: 비로그인 시 API 호출 없이 안내 메시지 렌더링
  if (!getToken()) {
    document.getElementById("foodGrid").innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🔒</div>
        <p>로그인 후 냉장고를 확인하세요!</p>
      </div>`;
    return;
  }

  const query = document.getElementById("searchInput").value.trim().toLowerCase();

  // Query Parameter 구성
  const params = new URLSearchParams();
  if (query) params.set("keyword", query);
  if (currentFilter !== "all") params.set("storage_location", toApiLocation(`🧊 ${currentFilter}`) || toApiLocation(`❄️ ${currentFilter}`) || toApiLocation(`🏠 ${currentFilter}`));

  // 필터 탭 → API enum 직접 매핑
  const filterToApi = { "냉장": "REFRIGERATED", "냉동": "FROZEN", "실온": "ROOM_TEMP" };
  if (currentFilter !== "all") {
    params.set("storage_location", filterToApi[currentFilter]);
  }

  try {
    const res = await authFetch(`${BASE_URL}/fridge-items?${params}`);
    if (!res.ok) throw new Error();

    const list = await res.json();
    const grid = document.getElementById("foodGrid");

    if (list.length === 0) {
      grid.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">🫙</div>
          <p>식품이 없습니다. 추가해보세요!</p>
        </div>`;
    } else {
      grid.innerHTML = list.map(item => buildFoodCard(item)).join("");
    }

    await updateSidebar();

  } catch (e) {
    showToast("⚠️ 식품 목록을 불러올 수 없습니다");
  }
}

/** 식품 카드 HTML 생성
 *  API 응답의 status, d_day, status_text 를 직접 사용
 *  status: "OK" | "WARNING" | "EXPIRED" → CSS 클래스: ok / expiring / expired
 */
function buildFoodCard(item) {
  const statusClass = { OK: "ok", WARNING: "expiring", EXPIRED: "expired" }[item.status] ?? "ok";
  const locFront    = fromApiLocation(item.storage_location); // "REFRIGERATED" → "🧊 냉장"
  const locText     = locFront.replace(/[^가-힣]/g, ""); // 이모지 제거 → "냉장"

  return `
    <div class="food-card ${statusClass}">
      <div class="days-badge ${statusClass}">${escapeHtml(item.status_text)}</div>
      <div class="food-info">
        <div class="food-name">${escapeHtml(item.name)}</div>
        <div class="food-meta">
          <span class="food-qty">${escapeHtml(item.quantity)}</span>
          <span class="location-badge loc-${escapeHtml(locText)}">${escapeHtml(locFront)}</span>
        </div>
      </div>
      <div class="food-actions">
        <button class="btn-ghost" title="수정"  onclick="openEditItem(${item.item_id})">✏️</button>
        <button class="btn-danger-ghost" title="삭제" onclick="deleteItem(${item.item_id})">🗑️</button>
      </div>
    </div>`;
}

// ── 사이드바 통계 업데이트 ───────────────────────────────────

/** GET /fridge-items/summary — 사이드바 통계/임박/만료 렌더링 */
async function updateSidebar() {
  // fix #76: 비로그인 시 사이드바 통계 API 호출 생략
  if (!getToken()) {
    ["stat-total", "stat-expiring", "stat-expired"].forEach(id => {
      document.getElementById(id).textContent = 0;
    });
    ["냉장", "냉동", "실온"].forEach(key => {
      document.getElementById("bar-" + key).style.width = "0%";
      document.getElementById("cnt-" + key).textContent = 0;
    });
    document.getElementById("soon-card").style.display = "none";
    document.getElementById("expired-card").style.display = "none";
    return;
  }

  try {
    const res = await authFetch(`${BASE_URL}/fridge-items/summary`);
    if (!res.ok) throw new Error();

    const data = await res.json();
    const total = data.total_count;

    // 숫자 통계
    document.getElementById("stat-total").textContent    = total;
    document.getElementById("stat-expiring").textContent = data.expiring_soon_count;
    document.getElementById("stat-expired").textContent  = data.expired_count;

    // 보관 위치 바
    const locMap = { 냉장: "REFRIGERATED", 냉동: "FROZEN", 실온: "ROOM_TEMP" };
    ["냉장", "냉동", "실온"].forEach(key => {
      const count = data.location_stats[locMap[key]] ?? 0;
      const pct   = total ? Math.round((count / total) * 100) : 0;
      document.getElementById("bar-" + key).style.width = pct + "%";
      document.getElementById("cnt-" + key).textContent = count;
    });

    // 곧 만료 카드
    const soonCard = document.getElementById("soon-card");
    const soonList = document.getElementById("soon-list");
    if (data.expiring_soon_items.length > 0) {
      soonCard.style.display = "";
      soonList.innerHTML = data.expiring_soon_items.map(i => `
        <div class="expired-item">
          <span>${escapeHtml(i.name)}</span>
          <span class="days" style="color:var(--yellow)">${escapeHtml(i.status_text)}</span>
        </div>`).join("");
    } else {
      soonCard.style.display = "none";
    }

    // 유통기한 지남 카드
    const expCard = document.getElementById("expired-card");
    const expList = document.getElementById("expired-list");
    if (data.expired_items.length > 0) {
      expCard.style.display = "";
      expList.innerHTML = data.expired_items.map(i => `
        <div class="expired-item">
          <span>${escapeHtml(i.name)}</span>
          <span class="days">${escapeHtml(i.status_text)}</span>
        </div>`).join("");
    } else {
      expCard.style.display = "none";
    }

  } catch (e) {
    showToast("⚠️ 사이드바 통계를 불러올 수 없습니다");
  }
}

// ── 레시피 모달 렌더링 ───────────────────────────────────────

/** GET /recipes/recommendations — AI 레시피 추천 */
async function renderRecipes() {
  const grid = document.getElementById("recipeGrid");
  grid.innerHTML = `<div class="recipe-empty"><p>🤖 AI가 레시피를 추천하는 중...</p></div>`;

  try {
    const res = await authFetch(`${BASE_URL}/recipes/recommendations`);
    if (!res.ok) throw new Error();

    const data = await res.json();
    const nowList = data.available_now        ?? [];
    const buyList = data.need_few_ingredients ?? [];

    document.getElementById("cnt-now").textContent = nowList.length + "개";
    document.getElementById("cnt-buy").textContent = buyList.length + "개";

    const allCards = [
      ...nowList.map(r => buildRecipeCard(r, false)),
      ...buyList.map(r => buildRecipeCard(r, true)),
    ];

    grid.innerHTML = allCards.length
      ? allCards.join("")
      : `<div class="recipe-empty">
           <div style="font-size:40px;margin-bottom:12px">🤔</div>
           <p>냉장고에 식품을 추가하면 레시피를 추천해드려요!</p>
         </div>`;

  } catch (e) {
    grid.innerHTML = `<div class="recipe-empty"><p>⚠️ 레시피를 불러올 수 없습니다</p></div>`;
  }
}

/** 레시피 카드 HTML 생성
 *  API 응답: { recipe_id, recipe_name, category, all_ingredients,
 *              missing_ingredients, instructions, expiring_ingredients_used }
 */
function buildRecipeCard(recipe, needBuy) {
  const stepsHtml = (recipe.instructions ?? [])
    .map((s, i) => `
      <div class="step-item">
        <div class="step-num">${i + 1}</div>
        <div>${escapeHtml(s)}</div>
      </div>`)
    .join("");

  const buyBox = (needBuy && recipe.missing_ingredients?.length > 0)
    ? `<div class="need-to-buy">
         <div class="label">🛒 구매 필요 재료</div>
         <div class="ingredient-tags">
           ${recipe.missing_ingredients.map(e => `<span class="ingredient-tag missing">${escapeHtml(e)}</span>`).join("")}
         </div>
       </div>`
    : "";

  const missing = new Set(recipe.missing_ingredients ?? []);

  return `
    <div class="recipe-card">
      <div class="recipe-card-header">
        <div class="recipe-avatar">🍽️</div>
        <div>
          <div class="recipe-name">${escapeHtml(recipe.recipe_name)}</div>
          <span class="category-tag">${escapeHtml(recipe.category)}</span>
        </div>
      </div>
      ${buyBox}
      <div>
        <div style="font-size:12px;color:var(--gray-400);margin-bottom:6px;">전체 재료</div>
        <div class="ingredient-tags">
          ${(recipe.all_ingredients ?? [])
            .map(e => `<span class="ingredient-tag ${missing.has(e) ? "missing" : ""}">${escapeHtml(e)}</span>`)
            .join("")}
        </div>
      </div>
      <button class="recipe-expand" onclick="toggleSteps(this)">조리법 보기 ▾</button>
      <div class="recipe-steps">
        <div class="step-list">${stepsHtml}</div>
      </div>
      <button
        class="select-btn"
        data-id="${escapeHtml(recipe.recipe_id)}"
        data-name="${escapeHtml(recipe.recipe_name)}"
        data-category="${escapeHtml(recipe.category)}"
        onclick="selectRecipe(this)">
        👍 선택했어요 👍
      </button>
    </div>`;
}

/** 조리법 토글 */
function toggleSteps(btn) {
  const steps = btn.nextElementSibling;
  steps.classList.toggle("open");
  btn.textContent = steps.classList.contains("open") ? "조리법 접기 ▴" : "조리법 보기 ▾";
}

/** 레시피 선택 → POST /user-recipes 저장 */
async function selectRecipe(btn) {
  if (!currentUser) {
    showToast("⚠️ 로그인 후 레시피를 선택할 수 있어요");
    openLogin();
    return;
  }

  const recipe_id   = Number(btn.dataset.id);
  const recipe_name = btn.dataset.name;
  const category    = btn.dataset.category;

  try {
    const res = await authFetch(`${BASE_URL}/user-recipes`, {
      method : "POST",
      body   : JSON.stringify({ recipe_id, recipe_name, category }),
    });

    if (!res.ok) {
      const err = await res.json();
      showToast(`⚠️ ${err.message ?? "저장에 실패했습니다"}`);
      return;
    }

    // UI 업데이트
    document.querySelectorAll(".select-btn").forEach(b => {
      b.classList.remove("selected");
      b.textContent = "👍 선택했어요 👍";
    });
    btn.classList.add("selected");
    btn.textContent = "✅ 선택됨!";

    showToast(`🍽️ "${recipe_name}" 레시피를 선택했어요!`);

  } catch (e) {
    showToast("⚠️ 서버에 연결할 수 없습니다");
  }
}

// ── 온보딩 렌더링 ────────────────────────────────────────────
let onboardStep = 0;
const onboardSelections = {}; // 단계별 선택값 저장

// 온보딩 선택값 → API enum 변환 맵
const CUISINE_TO_API = {
  "🍚 한식" : "KOREAN",
  "🍜 일식" : "JAPANESE",
  "🍝 양식" : "WESTERN",
  "🌮 중식" : "CHINESE",
};
const DIFFICULTY_TO_API = {
  "😊 쉬움"        : { difficulty_preference: "EASY",   quick_meal_preferred: false },
  "💪 보통"        : { difficulty_preference: "NORMAL", quick_meal_preferred: false },
  "🔥 어려워도 OK" : { difficulty_preference: "HARD",   quick_meal_preferred: false },
  "⚡ 간편식 선호" : { difficulty_preference: "EASY",   quick_meal_preferred: true  },
};

function renderOnboard() {
  const step = ONBOARD_STEPS[onboardStep];
  document.getElementById("ob-step").textContent = `${onboardStep + 1} / ${ONBOARD_STEPS.length}`;
  document.getElementById("ob-q").textContent = step.question;

  const wrap = document.getElementById("ob-options");
  wrap.innerHTML = step.options
    .map(opt => `<button class="onboard-option" onclick="toggleOnboard(this)">${opt}</button>`)
    .join("");

  const nextBtn = document.getElementById("ob-next-btn");
  nextBtn.textContent = onboardStep < ONBOARD_STEPS.length - 1 ? "다음" : "시작하기";
}

function toggleOnboard(el) {
  if (!ONBOARD_STEPS[onboardStep].multi) {
    document.querySelectorAll(".onboard-option").forEach(b => b.classList.remove("selected"));
  }
  el.classList.toggle("selected");
}

async function onboardNext() {
  // 현재 단계 선택값 저장
  const selected = [...document.querySelectorAll(".onboard-option.selected")]
    .map(b => b.textContent);
  onboardSelections[onboardStep] = selected;

  onboardStep++;
  if (onboardStep >= ONBOARD_STEPS.length) {
    // 마지막 단계 — PUT /users/me/preferences 호출
    await savePreferences();
    closeModal("onboardModal");
    showToast("설정 완료! 스마트 냉장고에 오신 걸 환영해요 🎉");
    return;
  }
  renderOnboard();
}

/** 온보딩 결과 → PUT /users/me/preferences */
async function savePreferences() {
  // step 0: 좋아하는 음식 종류 (multi)
  const favorite_cuisines = (onboardSelections[0] ?? [])
    .map(v => CUISINE_TO_API[v])
    .filter(Boolean);

  // step 1: 조리 난이도 (single)
  const diffKey = (onboardSelections[1] ?? [])[0] ?? "";
  const { difficulty_preference, quick_meal_preferred } =
    DIFFICULTY_TO_API[diffKey] ?? { difficulty_preference: "NORMAL", quick_meal_preferred: false };

  try {
    const res = await authFetch(`${BASE_URL}/users/me/preferences`, {
      method : "PUT",
      body   : JSON.stringify({ favorite_cuisines, difficulty_preference, quick_meal_preferred }),
    });
    if (!res.ok) throw new Error();
  } catch (e) {
    // 취향 저장 실패해도 온보딩 진행은 막지 않음
    console.warn("취향 저장 실패:", e);
  }
}