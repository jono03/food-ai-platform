package com.example.foodaiplatformserver.fridgeitem.controller;

import com.example.foodaiplatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.fridgeitem.repository.FridgeItemRepository;
import com.example.foodaiplatformserver.user.repository.UserPreferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class FridgeItemControllerTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private FridgeItemRepository fridgeItemRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        fridgeItemRepository.deleteAll();
        userPreferenceRepository.deleteAll();
        userAccountRepository.deleteAll();
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("냉장고 현황 요약을 반환한다")
    @Test
    void returnsSummary() throws Exception {
        AuthSession user = signupAndLogin("summary@example.com");

        saveFridgeItem(user.userId(), "당근", "1개", StorageLocation.REFRIGERATED, 1);
        saveFridgeItem(user.userId(), "만두", "10개", StorageLocation.FROZEN, -2);
        saveFridgeItem(user.userId(), "양파", "2개", StorageLocation.ROOM_TEMP, 10);

        mockMvc.perform(get("/fridge-items/summary")
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(3))
                .andExpect(jsonPath("$.expiring_soon_count").value(1))
                .andExpect(jsonPath("$.expired_count").value(1))
                .andExpect(jsonPath("$.location_stats.REFRIGERATED").value(1))
                .andExpect(jsonPath("$.location_stats.FROZEN").value(1))
                .andExpect(jsonPath("$.location_stats.ROOM_TEMP").value(1))
                .andExpect(jsonPath("$.expiring_soon_items", hasSize(1)))
                .andExpect(jsonPath("$.expired_items", hasSize(1)));
    }

    @DisplayName("로그인 사용자의 식재료 전체 목록을 유통기한 임박순으로 조회한다")
    @Test
    void getFridgeItemsOrderedByExpirationDate() throws Exception {
        AuthSession user = signupAndLogin("user@example.com");
        AuthSession otherUser = signupAndLogin("other@example.com");

        saveFridgeItem(user.userId(), "당근", "1개", StorageLocation.REFRIGERATED, 7);
        saveFridgeItem(user.userId(), "대파", "100g", StorageLocation.REFRIGERATED, 1);
        saveFridgeItem(otherUser.userId(), "외부데이터", "1개", StorageLocation.ROOM_TEMP, 0);

        mockMvc.perform(get("/fridge-items")
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("대파"))
                .andExpect(jsonPath("$[0].status").value("WARNING"))
                .andExpect(jsonPath("$[0].d_day").value(1))
                .andExpect(jsonPath("$[1].name").value("당근"));
    }

    @DisplayName("검색어, 보관 위치, 상태 필터를 조합해서 조회할 수 있다")
    @Test
    void getFridgeItemsWithCombinedFilters() throws Exception {
        AuthSession user = signupAndLogin("filter@example.com");

        saveFridgeItem(user.userId(), "대파", "100g", StorageLocation.REFRIGERATED, 2);
        saveFridgeItem(user.userId(), "대패삼겹살", "300g", StorageLocation.FROZEN, 10);
        saveFridgeItem(user.userId(), "대추", "10개", StorageLocation.REFRIGERATED, -1);

        mockMvc.perform(get("/fridge-items")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .queryParam("keyword", "대")
                        .queryParam("storage_location", "REFRIGERATED")
                        .queryParam("status", "WARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("대파"))
                .andExpect(jsonPath("$[0].storage_location").value("REFRIGERATED"))
                .andExpect(jsonPath("$[0].status").value("WARNING"));
    }

    @DisplayName("식재료 등록에 성공하면 201과 생성된 식재료를 반환한다")
    @Test
    void createFridgeItem() throws Exception {
        AuthSession user = signupAndLogin("create@example.com");
        LocalDate expirationDate = LocalDate.now(BUSINESS_ZONE).plusDays(13);

        mockMvc.perform(post("/fridge-items")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "우유",
                                  "quantity": "500ml",
                                  "storage_location": "REFRIGERATED",
                                  "expiration_date": "%s"
                                }
                                """.formatted(expirationDate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item_id").isNumber())
                .andExpect(jsonPath("$.name").value("우유"))
                .andExpect(jsonPath("$.quantity").value("500ml"))
                .andExpect(jsonPath("$.storage_location").value("REFRIGERATED"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.d_day").value(13))
                .andExpect(jsonPath("$.status_text").value("D-13"));
    }

    @DisplayName("식재료 등록 시 길이 제한을 넘으면 400을 반환한다")
    @Test
    void createFridgeItemRejectsTooLongFields() throws Exception {
        AuthSession user = signupAndLogin("length@example.com");
        String longName = "가".repeat(101);
        String longQuantity = "1".repeat(51);

        mockMvc.perform(post("/fridge-items")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "quantity": "%s",
                                  "storage_location": "REFRIGERATED",
                                  "expiration_date": "%s"
                                }
                                """.formatted(
                                longName,
                                longQuantity,
                                LocalDate.now(BUSINESS_ZONE).plusDays(13)
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details[*].field").value(org.hamcrest.Matchers.hasItems("name", "quantity")));
    }

    @DisplayName("본인 식재료 수정에 성공하면 200과 수정된 식재료를 반환한다")
    @Test
    void updateOwnedFridgeItem() throws Exception {
        AuthSession user = signupAndLogin("update@example.com");
        FridgeItem fridgeItem = saveFridgeItem(user.userId(), "우유", "500ml", StorageLocation.REFRIGERATED, 5);
        LocalDate updatedExpirationDate = LocalDate.now(BUSINESS_ZONE).plusDays(14);

        mockMvc.perform(put("/fridge-items/{itemId}", fridgeItem.getId())
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "저지방 우유",
                                  "quantity": "450ml",
                                  "storage_location": "REFRIGERATED",
                                  "expiration_date": "%s"
                                }
                                """.formatted(updatedExpirationDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item_id").value(fridgeItem.getId()))
                .andExpect(jsonPath("$.name").value("저지방 우유"))
                .andExpect(jsonPath("$.quantity").value("450ml"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.d_day").value(14))
                .andExpect(jsonPath("$.status_text").value("D-14"));
    }

    @DisplayName("타인의 식재료 수정은 403을 반환한다")
    @Test
    void updateOtherUsersFridgeItemFails() throws Exception {
        AuthSession user = signupAndLogin("user1@example.com");
        AuthSession otherUser = signupAndLogin("user2@example.com");
        FridgeItem fridgeItem = saveFridgeItem(otherUser.userId(), "사과", "2개", StorageLocation.ROOM_TEMP, 2);

        mockMvc.perform(put("/fridge-items/{itemId}", fridgeItem.getId())
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "청사과",
                                  "quantity": "3개",
                                  "storage_location": "ROOM_TEMP",
                                  "expiration_date": "%s"
                                }
                                """.formatted(LocalDate.now(BUSINESS_ZONE).plusDays(3))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @DisplayName("존재하지 않는 식재료 수정은 404를 반환한다")
    @Test
    void updateMissingFridgeItemFails() throws Exception {
        AuthSession user = signupAndLogin("missing-update@example.com");

        mockMvc.perform(put("/fridge-items/{itemId}", 9999L)
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "우유",
                                  "quantity": "500ml",
                                  "storage_location": "REFRIGERATED",
                                  "expiration_date": "%s"
                                }
                                """.formatted(LocalDate.now(BUSINESS_ZONE).plusDays(5))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @DisplayName("본인 식재료 삭제에 성공하면 204를 반환한다")
    @Test
    void deleteOwnedFridgeItem() throws Exception {
        AuthSession user = signupAndLogin("delete@example.com");
        FridgeItem fridgeItem = saveFridgeItem(user.userId(), "우유", "500ml", StorageLocation.REFRIGERATED, 5);

        mockMvc.perform(delete("/fridge-items/{itemId}", fridgeItem.getId())
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isNoContent());
    }

    @DisplayName("타인의 식재료 삭제는 403을 반환한다")
    @Test
    void deleteOtherUsersFridgeItemFails() throws Exception {
        AuthSession user = signupAndLogin("delete-user1@example.com");
        AuthSession otherUser = signupAndLogin("delete-user2@example.com");
        FridgeItem fridgeItem = saveFridgeItem(otherUser.userId(), "사과", "2개", StorageLocation.ROOM_TEMP, 2);

        mockMvc.perform(delete("/fridge-items/{itemId}", fridgeItem.getId())
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @DisplayName("존재하지 않는 식재료 삭제는 404를 반환한다")
    @Test
    void deleteMissingFridgeItemFails() throws Exception {
        AuthSession user = signupAndLogin("missing-delete@example.com");

        mockMvc.perform(delete("/fridge-items/{itemId}", 9999L)
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private FridgeItem saveFridgeItem(Long userId,
                                      String name,
                                      String quantity,
                                      StorageLocation storageLocation,
                                      int expirationOffsetDays) {
        return fridgeItemRepository.save(new FridgeItem(
                userId,
                name,
                quantity,
                storageLocation,
                LocalDate.of(2026, 5, 1),
                LocalDate.now(BUSINESS_ZONE).plusDays(expirationOffsetDays)
        ));
    }

    private AuthSession signupAndLogin(String email) throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "password123!"
                                }
                                """.formatted(email, email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        return new AuthSession(
                jsonNode.get("access_token").asText(),
                jsonNode.get("user").get("user_id").asLong()
        );
    }

    private record AuthSession(
            String accessToken,
            Long userId
    ) {
    }
}
