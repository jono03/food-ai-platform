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

// ── 식품 목록 렌더링 ─────────────────────────────────────────
function renderItems() {
  const query = document.getElementById("searchInput").value.trim().toLowerCase();

  // 1) 필터
  let list = [...fridgeItems];
  if (currentFilter !== "all") {
    list = list.filter(item => locLabel(item.storage_location) === currentFilter);
  }
  // 2) 검색
  if (query) {
    list = list.filter(item => item.name.toLowerCase().includes(query));
  }
  // 3) 유통기한 임박순 정렬
  list.sort((a, b) => getDaysLeft(a.expiration_date) - getDaysLeft(b.expiration_date));

  const grid = document.getElementById("foodGrid");

  if (list.length === 0) {
    grid.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🫙</div>
        <p>식품이 없습니다. 추가해보세요!</p>
      </div>`;
    updateSidebar();
    return;
  }

  grid.innerHTML = list.map(item => buildFoodCard(item)).join("");
  updateSidebar();
}

/** 식품 카드 HTML 생성 */
function buildFoodCard(item) {
  const days    = getDaysLeft(item.expiration_date);
  const status  = getStatus(days);
  const locText = locLabel(item.storage_location);

  return `
    <div class="food-card ${status}">
      <div class="days-badge ${status}">${formatDays(days)}</div>
      <div class="food-info">
        <div class="food-name">${item.name}</div>
        <div class="food-meta">
          <span class="food-qty">${item.quantity}</span>
          <span class="location-badge loc-${locText}">${item.storage_location}</span>
        </div>
      </div>
      <div class="food-actions">
        <button class="btn-ghost" title="수정"  onclick="openEditItem(${item.item_id})">✏️</button>
        <button class="btn-danger-ghost" title="삭제" onclick="deleteItem(${item.item_id})">🗑️</button>
      </div>
    </div>`;
}

// ── 사이드바 통계 업데이트 ───────────────────────────────────
function updateSidebar() {
  const total    = fridgeItems.length;
  const expired  = fridgeItems.filter(i => getDaysLeft(i.expiration_date) < 0);
  const expiring = fridgeItems.filter(i => {
    const d = getDaysLeft(i.expiration_date);
    return d >= 0 && d <= 3;
  });

  // 숫자 통계
  document.getElementById("stat-total").textContent    = total;
  document.getElementById("stat-expiring").textContent = expiring.length;
  document.getElementById("stat-expired").textContent  = expired.length;

  // 보관 위치 바
  const locCount = { 냉장: 0, 냉동: 0, 실온: 0 };
  fridgeItems.forEach(i => {
    const key = locLabel(i.storage_location);
    if (key in locCount) locCount[key]++;
  });
  ["냉장", "냉동", "실온"].forEach(key => {
    const pct = total ? Math.round((locCount[key] / total) * 100) : 0;
    document.getElementById("bar-" + key).style.width = pct + "%";
    document.getElementById("cnt-" + key).textContent = locCount[key];
  });

  // 곧 만료 카드
  const soonCard = document.getElementById("soon-card");
  const soonList = document.getElementById("soon-list");
  if (expiring.length > 0) {
    soonCard.style.display = "";
    soonList.innerHTML = expiring.map(i => {
      const d = getDaysLeft(i.expiration_date);
      return `
        <div class="expired-item">
          <span>${i.name}</span>
          <span class="days" style="color:var(--yellow)">
            ${d === 0 ? "오늘 만료" : d + "일 남음"}
          </span>
        </div>`;
    }).join("");
  } else {
    soonCard.style.display = "none";
  }

  // 유통기한 지남 카드
  const expCard = document.getElementById("expired-card");
  const expList = document.getElementById("expired-list");
  if (expired.length > 0) {
    expCard.style.display = "";
    expList.innerHTML = expired.map(i => `
      <div class="expired-item">
        <span>${i.name}</span>
        <span class="days">${Math.abs(getDaysLeft(i.expiration_date))}일 지남</span>
      </div>`).join("");
  } else {
    expCard.style.display = "none";
  }
}

// ── 레시피 모달 렌더링 ───────────────────────────────────────
function renderRecipes() {
  const myItems = fridgeItems.map(i => i.name.trim());

  const nowList = [];   // 지금 바로 만들 수 있는
  const buyList = [];   // 재료 조금만 사면 가능

  RECIPE_POOL.forEach(recipe => {
    const missingNeed = recipe.need.filter(n => !myItems.includes(n.trim()));
    const hasAllNeeded = missingNeed.length === 0;

    if (hasAllNeeded && recipe.extra.length === 0) {
      nowList.push(recipe);
    } else if (hasAllNeeded && recipe.extra.length > 0) {
      buyList.push(recipe);
    } else if (missingNeed.length <= 1) {
      // need 중 1개만 없어도 buy 목록에 추가 (extra에 missing 포함)
      buyList.push({ ...recipe, extra: [...recipe.extra, ...missingNeed] });
    }
  });

  document.getElementById("cnt-now").textContent = nowList.length + "개";
  document.getElementById("cnt-buy").textContent = buyList.length + "개";

  const allCards = [
    ...nowList.map(r => buildRecipeCard(r, false)),
    ...buyList.map(r => buildRecipeCard(r, true)),
  ];

  const grid = document.getElementById("recipeGrid");
  grid.innerHTML = allCards.length
    ? allCards.join("")
    : `<div class="recipe-empty">
         <div style="font-size:40px;margin-bottom:12px">🤔</div>
         <p>냉장고에 식품을 추가하면 레시피를 추천해드려요!</p>
       </div>`;
}

/** 레시피 카드 HTML 생성 */
function buildRecipeCard(recipe, needBuy) {
  const stepsHtml = recipe.steps
    .map((s, i) => `
      <div class="step-item">
        <div class="step-num">${i + 1}</div>
        <div>${s}</div>
      </div>`)
    .join("");

  const buyBox = (needBuy && recipe.extra.length > 0)
    ? `<div class="need-to-buy">
         <div class="label">🛒 구매 필요 재료</div>
         <div class="ingredient-tags">
           ${recipe.extra.map(e => `<span class="ingredient-tag missing">${e}</span>`).join("")}
         </div>
       </div>`
    : "";

  const allIngr = [...recipe.need, ...recipe.extra];

  // USER_RECIPE 저장 시 사용할 정보를 data 속성으로 넘김
  return `
    <div class="recipe-card">
      <div class="recipe-card-header">
        <div class="recipe-avatar">${recipe.icon}</div>
        <div>
          <div class="recipe-name">${recipe.recipe_name}</div>
          <span class="category-tag">${recipe.category}</span>
        </div>
      </div>
      ${buyBox}
      <div>
        <div style="font-size:12px;color:var(--gray-400);margin-bottom:6px;">전체 재료</div>
        <div class="ingredient-tags">
          ${allIngr
            .map(e => `<span class="ingredient-tag ${recipe.extra.includes(e) ? "missing" : ""}">${e}</span>`)
            .join("")}
        </div>
      </div>
      <button class="recipe-expand" onclick="toggleSteps(this)">조리법 보기 ▾</button>
      <div class="recipe-steps">
        <div class="step-list">${stepsHtml}</div>
      </div>
      <button
        class="select-btn"
        data-name="${recipe.recipe_name}"
        data-category="${recipe.category}"
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

/** 레시피 선택 → USER_RECIPE에 저장 */
function selectRecipe(btn) {
  // 로그인 체크
  if (!currentUser) {
    showToast("⚠️ 로그인 후 레시피를 선택할 수 있어요");
    openLogin();
    return;
  }

  const recipe_name = btn.dataset.name;
  const category    = btn.dataset.category;

  // USER_RECIPE 엔티티에 추가 (실제 서비스: POST /api/user-recipes)
  userRecipes.push({
    recipe_id   : nextRecipeId++,
    user_id     : currentUser.user_id,   // 로그인된 사용자 ID만 사용
    recipe_name,
    category,
  });

  // UI 업데이트
  document.querySelectorAll(".select-btn").forEach(b => {
    b.classList.remove("selected");
    b.textContent = "👍 선택했어요 👍";
  });
  btn.classList.add("selected");
  btn.textContent = "✅ 선택됨!";

  showToast(`🍽️ "${recipe_name}" 레시피를 선택했어요!`);
  console.log("저장된 USER_RECIPE:", userRecipes); // 백엔드 연결 전 확인용
}

// ── 온보딩 렌더링 ────────────────────────────────────────────
let onboardStep = 0;

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

function onboardNext() {
  onboardStep++;
  if (onboardStep >= ONBOARD_STEPS.length) {
    closeModal("onboardModal");
    showToast("설정 완료! 스마트 냉장고에 오신 걸 환영해요 🎉");
    return;
  }
  renderOnboard();
}