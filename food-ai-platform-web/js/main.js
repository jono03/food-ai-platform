// ============================================================
//  main.js  —  페이지 로드 후 초기화 & 이벤트 등록
// ============================================================

document.addEventListener("DOMContentLoaded", () => {

  // ── 버튼 이벤트 등록 ───────────────────────────────────────
  // loginBtn 핸들러는 updateAuthButton()에서 로그인 상태에 따라 동적으로 설정
  // fix #83: 아래 고정 바인딩 제거 → modal.js updateAuthButton()으로 일원화
  // document.getElementById("loginBtn").addEventListener("click", openLogin);
  document.getElementById("addItemBtn") .addEventListener("click", openAddItem);
  document.getElementById("recipeBtn")  .addEventListener("click", openRecipe);
  document.getElementById("doLoginBtn") .addEventListener("click", doLogin);
  document.getElementById("doSignupBtn").addEventListener("click", doSignup);
  document.getElementById("submitItemBtn").addEventListener("click", submitItem);
  document.getElementById("ob-next-btn").addEventListener("click", onboardNext);

  // 모달 닫기 버튼 (data-close 속성으로 대상 지정)
  document.querySelectorAll("[data-close]").forEach(btn => {
    btn.addEventListener("click", () => closeModal(btn.dataset.close));
  });

  // 모달 오버레이 클릭 시 닫기
  bindOverlayClose();

  // ── 검색창 ─────────────────────────────────────────────────
  document.getElementById("searchInput").addEventListener("input", renderItems);

  // ── 필터 탭 ────────────────────────────────────────────────
  document.querySelectorAll(".filter-tab").forEach(tab => {
    tab.addEventListener("click", () => {
      document.querySelectorAll(".filter-tab").forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      currentFilter = tab.dataset.filter;
      renderItems();
    });
  });

  // ── 초기 렌더링 ────────────────────────────────────────────
  renderItems();       // 식품 목록
  renderOnboard();     // 온보딩 설문
  updateAuthButton();  // fix #83: 초기 로그인 버튼 핸들러 등록

  // 온보딩 모달 열기
  openModal("onboardModal");
});