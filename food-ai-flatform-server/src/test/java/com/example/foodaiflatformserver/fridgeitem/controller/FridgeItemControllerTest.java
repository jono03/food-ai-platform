package com.example.foodaiflatformserver.fridgeitem.controller;

import com.example.foodaiflatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiflatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiflatformserver.fridgeitem.repository.FridgeItemRepository;
import com.example.foodaiflatformserver.fridgeitem.service.FridgeItemStatusCalculator;
import com.example.foodaiflatformserver.user.entity.User;
import com.example.foodaiflatformserver.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class FridgeItemControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FridgeItemRepository fridgeItemRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        fridgeItemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("로그인 사용자의 식재료 전체 목록을 유통기한 임박순으로 조회한다")
    @Test
    void getFridgeItemsOrderedByExpirationDate() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        User otherUser = userRepository.save(User.builder()
                .username("다른사용자")
                .email("other@example.com")
                .password("encoded")
                .build());

        fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("당근")
                .quantity("1개")
                .storageLocation(StorageLocation.REFRIGERATED)
                .registeredDate(LocalDate.of(2026, 5, 1))
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(7))
                .build());
        fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("대파")
                .quantity("100g")
                .storageLocation(StorageLocation.REFRIGERATED)
                .registeredDate(LocalDate.of(2026, 5, 1))
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(1))
                .build());
        fridgeItemRepository.save(FridgeItem.builder()
                .user(otherUser)
                .name("외부데이터")
                .quantity("1개")
                .storageLocation(StorageLocation.ROOM_TEMP)
                .registeredDate(LocalDate.of(2026, 5, 1))
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(0))
                .build());

        mockMvc.perform(get("/api/v1/fridge-items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("대파"))
                .andExpect(jsonPath("$[0].status").value("WARNING"))
                .andExpect(jsonPath("$[0].d_day").value(1))
                .andExpect(jsonPath("$[1].name").value("당근"));
    }

    @DisplayName("검색어, 보관 위치, 상태 필터를 조합해서 조회할 수 있다")
    @Test
    void getFridgeItemsWithCombinedFilters() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        User user = userRepository.findByEmail("user@example.com").orElseThrow();

        fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("대파")
                .quantity("100g")
                .storageLocation(StorageLocation.REFRIGERATED)
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(2))
                .build());
        fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("대패삼겹살")
                .quantity("300g")
                .storageLocation(StorageLocation.FROZEN)
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(10))
                .build());
        fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("대추")
                .quantity("10개")
                .storageLocation(StorageLocation.REFRIGERATED)
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).minusDays(1))
                .build());

        mockMvc.perform(get("/api/v1/fridge-items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .queryParam("keyword", "대")
                        .queryParam("storage_location", "REFRIGERATED")
                        .queryParam("status", "WARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("대파"))
                .andExpect(jsonPath("$[0].storage_location").value("REFRIGERATED"))
                .andExpect(jsonPath("$[0].status").value("WARNING"));
    }

    @DisplayName("식재료 등록에 성공하면 201과 생성된 식재료를 반환한다")
    @Test
    void createFridgeItem() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        LocalDate expirationDate = LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(13);

        mockMvc.perform(post("/api/v1/fridge-items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
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
        String accessToken = signupAndLogin("user@example.com");
        String longName = "가".repeat(101);
        String longQuantity = "1".repeat(51);

        mockMvc.perform(post("/api/v1/fridge-items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
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
                                LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(13)
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details[0].field").value("name"))
                .andExpect(jsonPath("$.details[0].reason").value("식품명은 100자 이하여야 합니다."))
                .andExpect(jsonPath("$.details[1].field").value("quantity"))
                .andExpect(jsonPath("$.details[1].reason").value("수량은 50자 이하여야 합니다."));
    }

    @DisplayName("본인 식재료 수정에 성공하면 200과 수정된 식재료를 반환한다")
    @Test
    void updateOwnedFridgeItem() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        FridgeItem fridgeItem = fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("우유")
                .quantity("500ml")
                .storageLocation(StorageLocation.REFRIGERATED)
                .registeredDate(LocalDate.of(2026, 5, 1))
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(5))
                .build());

        LocalDate updatedExpirationDate = LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(14);

        mockMvc.perform(put("/api/v1/fridge-items/{itemId}", fridgeItem.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
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
                .andExpect(jsonPath("$.registered_date").value("2026-05-01"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.d_day").value(14))
                .andExpect(jsonPath("$.status_text").value("D-14"));
    }

    @DisplayName("타인의 식재료 수정은 403을 반환한다")
    @Test
    void updateOtherUsersFridgeItemFails() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        signupAndLogin("other@example.com");

        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();
        FridgeItem fridgeItem = fridgeItemRepository.save(FridgeItem.builder()
                .user(otherUser)
                .name("사과")
                .quantity("2개")
                .storageLocation(StorageLocation.ROOM_TEMP)
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(2))
                .build());

        mockMvc.perform(put("/api/v1/fridge-items/{itemId}", fridgeItem.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "청사과",
                                  "quantity": "3개",
                                  "storage_location": "ROOM_TEMP",
                                  "expiration_date": "%s"
                                }
                                """.formatted(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(3))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @DisplayName("존재하지 않는 식재료 수정은 404를 반환한다")
    @Test
    void updateMissingFridgeItemFails() throws Exception {
        String accessToken = signupAndLogin("user@example.com");

        mockMvc.perform(put("/api/v1/fridge-items/{itemId}", 9999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "우유",
                                  "quantity": "500ml",
                                  "storage_location": "REFRIGERATED",
                                  "expiration_date": "%s"
                                }
                                """.formatted(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(5))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @DisplayName("본인 식재료 삭제에 성공하면 204를 반환한다")
    @Test
    void deleteOwnedFridgeItem() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        FridgeItem fridgeItem = fridgeItemRepository.save(FridgeItem.builder()
                .user(user)
                .name("우유")
                .quantity("500ml")
                .storageLocation(StorageLocation.REFRIGERATED)
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(5))
                .build());

        mockMvc.perform(delete("/api/v1/fridge-items/{itemId}", fridgeItem.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @DisplayName("타인의 식재료 삭제는 403을 반환한다")
    @Test
    void deleteOtherUsersFridgeItemFails() throws Exception {
        String accessToken = signupAndLogin("user@example.com");
        signupAndLogin("other@example.com");

        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();
        FridgeItem fridgeItem = fridgeItemRepository.save(FridgeItem.builder()
                .user(otherUser)
                .name("사과")
                .quantity("2개")
                .storageLocation(StorageLocation.ROOM_TEMP)
                .expirationDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE).plusDays(2))
                .build());

        mockMvc.perform(delete("/api/v1/fridge-items/{itemId}", fridgeItem.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @DisplayName("존재하지 않는 식재료 삭제는 404를 반환한다")
    @Test
    void deleteMissingFridgeItemFails() throws Exception {
        String accessToken = signupAndLogin("user@example.com");

        mockMvc.perform(delete("/api/v1/fridge-items/{itemId}", 9999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private String signupAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "홍길동",
                          "email": "%s",
                          "password": "Password123!"
                        }
                        """.formatted(email)));

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        return jsonNode.get("access_token").asText();
    }
}
