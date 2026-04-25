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

// ── 로그인 ───────────────────────────────────────────────────

function openLogin() {
  openModal("loginModal");
}

function doLogin() {
  const email    = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPw").value;

  if (!email || !password) {
    showToast("⚠️ 이메일과 비밀번호를 입력해주세요");
    return;
  }

  // 실제 서비스: POST /api/auth/login { email, password }
  // 여기선 임시로 USER 엔티티 구조대로 객체 세팅
  currentUser = {
    user_id  : 1,
    username : email.split("@")[0],
    email    : email,
  };

  closeModal("loginModal");
  showToast("✅ 로그인되었습니다!");

  // 헤더 버튼 텍스트 변경
  document.getElementById("loginBtn").textContent = `👤 ${currentUser.username}`;

  console.log("로그인된 USER:", currentUser); // 백엔드 연결 전 확인용
}

// ── 식품 추가/수정 모달 ──────────────────────────────────────

function openAddItem() {
  editingItemId = null;

  // 폼 초기화
  document.getElementById("itemModalTitle").textContent = "🥗 식품 추가";
  document.getElementById("f-name").value = "";
  document.getElementById("f-qty").value  = "";
  document.getElementById("f-loc").value  = "🧊 냉장";
  document.getElementById("f-exp").value  = "";
  document.getElementById("submitItemBtn").textContent = "+ 추가하기";

  openModal("itemModal");
}

function openEditItem(item_id) {
  const item = fridgeItems.find(i => i.item_id === item_id);
  if (!item) return;

  editingItemId = item_id;

  // 폼에 기존 값 채우기
  document.getElementById("itemModalTitle").textContent = "✏️ 식품 수정";
  document.getElementById("f-name").value = item.name;
  document.getElementById("f-qty").value  = item.quantity;
  document.getElementById("f-loc").value  = item.storage_location;
  document.getElementById("f-exp").value  = item.expiration_date;
  document.getElementById("submitItemBtn").textContent = "💾 저장하기";

  openModal("itemModal");
}

function submitItem() {
  const name             = document.getElementById("f-name").value.trim();
  const quantity         = document.getElementById("f-qty").value.trim();
  const storage_location = document.getElementById("f-loc").value;
  const expiration_date  = document.getElementById("f-exp").value;

  if (!name || !quantity || !expiration_date) {
    showToast("⚠️ 모든 항목을 입력해주세요");
    return;
  }

  const today = new Date().toISOString().split("T")[0];

  if (editingItemId !== null) {
    // ── 수정 (실제 서비스: PUT /api/fridge-items/:item_id) ──
    const item = fridgeItems.find(i => i.item_id === editingItemId);
    item.name             = name;
    item.quantity         = quantity;
    item.storage_location = storage_location;
    item.expiration_date  = expiration_date;

    showToast("✅ 식품이 수정되었습니다");
  } else {
    // ── 추가 (실제 서비스: POST /api/fridge-items) ──────────
    // FRIDGE_ITEM 엔티티 구조 그대로 생성
    const newItem = {
      item_id          : nextItemId++,
      user_id          : currentUser ? currentUser.user_id : 1,
      name,
      quantity,
      storage_location,
      registered_date  : today,
      expiration_date,
    };
    fridgeItems.push(newItem);

    showToast("✅ 식품이 추가되었습니다");
    console.log("추가된 FRIDGE_ITEM:", newItem); // 백엔드 연결 전 확인용
  }

  closeModal("itemModal");
  renderItems();
}

function deleteItem(item_id) {
  if (!confirm("삭제하시겠습니까?")) return;

  // 실제 서비스: DELETE /api/fridge-items/:item_id
  fridgeItems = fridgeItems.filter(i => i.item_id !== item_id);

  renderItems();
  showToast("🗑️ 삭제되었습니다");
}

// ── 레시피 모달 ──────────────────────────────────────────────

function openRecipe() {
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
