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
