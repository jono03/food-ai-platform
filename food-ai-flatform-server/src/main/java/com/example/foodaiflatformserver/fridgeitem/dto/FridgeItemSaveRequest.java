package com.example.foodaiflatformserver.fridgeitem.dto;

import com.example.foodaiflatformserver.fridgeitem.entity.StorageLocation;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FridgeItemSaveRequest(
        @JsonProperty("name")
        @NotBlank(message = "식품명은 필수입니다.")
        String name,

        @JsonProperty("quantity")
        @NotBlank(message = "수량은 필수입니다.")
        String quantity,

        @JsonProperty("storage_location")
        @NotNull(message = "보관 위치는 필수입니다.")
        StorageLocation storageLocation,

        @JsonProperty("expiration_date")
        @NotNull(message = "유통기한은 필수입니다.")
        LocalDate expirationDate
) {
}
