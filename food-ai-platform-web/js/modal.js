// ============================================================
//  modal.js  —  모달 열기/닫기 & 식품 CRUD & 로그인
// ============================================================

let editingItemId = null; // 수정 중인 item_id (null이면 추가 모드)

// ── 모달 공통 ────────────────────────────────────────────────

function openModal(id) {
  document.getElementById(id).classList.add("open");
}

function closeModal(id) {
  document.getElementById(id).classList.remove("open");
}

// 오버레이 클릭 시 닫기 (index.html 로드 후 main.js에서 등록)
function bindOverlayClose() {
  document.querySelectorAll(".modal-overlay").forEach(overlay => {
    overlay.addEventListener("click", e => {
      if (e.target === overlay) overlay.classList.remove("open");
    });
  });
}

// ── JWT 토큰 유틸 ────────────────────────────────────────────

function getToken()        { return sessionStorage.getItem("access_token"); }
function setToken(token)   { sessionStorage.setItem("access_token", token); }
function removeToken()     { sessionStorage.removeItem("access_token"); }

/**
 * 인증이 필요한 API 요청 공통 함수
 * Authorization 헤더를 자동으로 추가
 */
async function authFetch(url, options = {}) {
  const token = getToken();
  return fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { "Authorization": `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  });
}

// ── 로그인 ───────────────────────────────────────────────────

function openLogin() {
  // fix #79: 브라우저 자동완성으로 채워진 값 초기화
  document.getElementById("loginEmail").value = "";
  document.getElementById("loginPw").value = "";
  openModal("loginModal");
}

async function doLogin() {
  const email    = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPw").value.trim();

  if (!email || !password) {
    showToast("⚠️ 이메일과 비밀번호를 입력해주세요");
    return;
  }

  try {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method  : "POST",
      headers : { "Content-Type": "application/json" },
      body    : JSON.stringify({ email, password }),
    });

    if (!res.ok) {
      const err = await res.json();
      showToast(`⚠️ ${err.message ?? "로그인에 실패했습니다"}`);
      return;
    }

    const data = await res.json();
    setToken(data.access_token);

    currentUser = {
      user_id  : data.user.user_id,
      username : data.user.username,
      email    : data.user.email,
    };

    closeModal("loginModal");
    showToast("✅ 로그인되었습니다!");
    updateAuthButton();

  } catch (e) {
    showToast("⚠️ 서버에 연결할 수 없습니다");
  }
}

// ── 로그아웃 ─────────────────────────────────────────────────

function doLogout() {
  currentUser = null;
  removeToken();
  updateAuthButton();
  showToast("👋 로그아웃되었습니다");
}

// 로그인 상태에 따라 헤더 버튼 텍스트 & 핸들러 동기화
function updateAuthButton() {
  const btn = document.getElementById("loginBtn");
  if (currentUser) {
    btn.textContent = `👤 ${currentUser.username} (로그아웃)`;
    btn.onclick = doLogout;  // fix #83: 로그아웃 핸들러로 교체
  } else {
    btn.textContent = "↗ 로그인";
    btn.onclick = openLogin; // fix #83: 로그인 핸들러로 복구
  }
}

// ── 회원가입 ─────────────────────────────────────────────────

function openSignup() {
  closeModal("loginModal");
  openModal("signupModal");
}

/** 비밀번호 규칙 실시간 체크 */
function checkPwRules(value) {
  const rules = {
    "rule-length"  : value.length >= 8 && value.length <= 20,
    "rule-letter"  : /[A-Za-z]/.test(value),
    "rule-number"  : /\d/.test(value),
    "rule-special" : /[^A-Za-z\d]/.test(value),
  };
  Object.entries(rules).forEach(([id, pass]) => {
    document.getElementById(id)?.classList.toggle("ok", pass);
  });
}

async function doSignup() {
  const username = document.getElementById("signupName").value.trim();
  const email    = document.getElementById("signupEmail").value.trim();
  const password = document.getElementById("signupPw").value.trim();

  if (!username || !email || !password) {
    showToast("⚠️ 모든 항목을 입력해주세요");
    return;
  }

  try {
    const res = await fetch(`${BASE_URL}/auth/signup`, {
      method  : "POST",
      headers : { "Content-Type": "application/json" },
      body    : JSON.stringify({ username, email, password }),
    });

    if (!res.ok) {
      const err = await res.json();
      showToast(`⚠️ ${err.message ?? "회원가입에 실패했습니다"}`);
      return;
    }

    closeModal("signupModal");
    showToast("✅ 회원가입 완료! 로그인해주세요");
    openLogin();

  } catch (e) {
    showToast("⚠️ 서버에 연결할 수 없습니다");
  }
}

// ── 식품 추가/수정 모달 ──────────────────────────────────────

function openAddItem() {
  // 로그인 체크
  if (!currentUser) {
    showToast("⚠️ 로그인 후 식품을 추가할 수 있어요");
    openLogin();
    return;
  }

  editingItemId = null;

  // 폼 초기화
  document.getElementById("itemModalTitle").textContent = "🥗 식품 추가";
  document.getElementById("f-name").value = "";
  document.getElementById("f-qty").value  = "";
  document.getElementById("f-loc").value  = "🧊 냉장";
  document.getElementById("f-exp").value  = "";
  const todayStr = new Date().toISOString().split("T")[0];
  document.getElementById("f-exp").min = todayStr;
  document.getElementById("submitItemBtn").textContent = "+ 추가하기";

  openModal("itemModal");
}

function openEditItem(item_id) {
  // 로그인 체크
  if (!currentUser) {
    showToast("⚠️ 로그인 후 식품을 수정할 수 있어요");
    openLogin();
    return;
  }

  const item = fridgeItems.find(i => i.item_id === item_id);
  if (!item) return;

  editingItemId = item_id;

  // 폼에 기존 값 채우기
  document.getElementById("itemModalTitle").textContent = "✏️ 식품 수정";
  document.getElementById("f-name").value = item.name;
  document.getElementById("f-qty").value  = item.quantity;
  document.getElementById("f-loc").value  = item.storage_location;
  const todayStr = new Date().toISOString().split("T")[0];
  document.getElementById("f-exp").min = todayStr;
  document.getElementById("f-exp").value =
  item.expiration_date >= todayStr ? item.expiration_date : "";
  document.getElementById("submitItemBtn").textContent = "💾 저장하기";

  openModal("itemModal");
}

async function submitItem() {
  if (!currentUser) {
    showToast("⚠️ 로그인이 필요합니다");
    return;
  }

  const name             = document.getElementById("f-name").value.trim().replace(/\s+/g, " ");
  const quantity         = document.getElementById("f-qty").value.trim();
  const storage_location = toApiLocation(document.getElementById("f-loc").value);
  const expiration_date  = document.getElementById("f-exp").value;

  if (!name || !quantity || !expiration_date) {
    showToast("⚠️ 모든 항목을 입력해주세요");
    return;
  }

  const today = new Date().toISOString().split("T")[0];
  if (expiration_date < today) {
    showToast("⚠️ 유통기한이 오늘 이전입니다. 확인해주세요.");
    return;
  }

  const body = JSON.stringify({ name, quantity, storage_location, expiration_date });

  try {
    if (editingItemId !== null) {
      // ── 수정: PUT /fridge-items/{item_id} ───────────────────
      const res = await authFetch(`${BASE_URL}/fridge-items/${editingItemId}`, {
        method : "PUT",
        body,
      });
      if (!res.ok) {
        const err = await res.json();
        showToast(`⚠️ ${err.message ?? "수정에 실패했습니다"}`);
        return;
      }
      showToast("✅ 식품이 수정되었습니다");
    } else {
      // ── 추가: POST /fridge-items ─────────────────────────────
      const res = await authFetch(`${BASE_URL}/fridge-items`, {
        method : "POST",
        body,
      });
      if (!res.ok) {
        const err = await res.json();
        showToast(`⚠️ ${err.message ?? "추가에 실패했습니다"}`);
        return;
      }
      showToast("✅ 식품이 추가되었습니다");
    }

    closeModal("itemModal");
    await renderItems(); // 서버 최신 데이터로 갱신

  } catch (e) {
    showToast("⚠️ 서버에 연결할 수 없습니다");
  }
}

async function deleteItem(item_id) {
  if (!currentUser) {
    showToast("⚠️ 로그인이 필요합니다");
    return;
  }

  if (!confirm("삭제하시겠습니까?")) return;

  try {
    // ── 삭제: DELETE /fridge-items/{item_id} ────────────────
    const res = await authFetch(`${BASE_URL}/fridge-items/${item_id}`, {
      method : "DELETE",
    });

    if (!res.ok && res.status !== 204) {
      const err = await res.json();
      showToast(`⚠️ ${err.message ?? "삭제에 실패했습니다"}`);
      return;
    }

    await renderItems(); // 서버 최신 데이터로 갱신
    showToast("🗑️ 삭제되었습니다");

  } catch (e) {
    showToast("⚠️ 서버에 연결할 수 없습니다");
  }
}

// ── 레시피 모달 ──────────────────────────────────────────────

function openRecipe() {
  // fix #82: 비로그인 시 레시피 추천 접근 차단
  if (!currentUser) {
    showToast("⚠️ 로그인 후 레시피를 추천받을 수 있어요");
    openLogin();
    return;
  }
  renderRecipes();
  openModal("recipeModal");
}

// ── 토스트 알림 ──────────────────────────────────────────────

function showToast(msg) {
  const toast = document.getElementById("toast");
  toast.textContent = msg;
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2500);
}