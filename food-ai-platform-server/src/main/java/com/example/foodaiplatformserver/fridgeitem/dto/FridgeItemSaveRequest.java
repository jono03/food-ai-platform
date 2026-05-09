package com.example.foodaiplatformserver.fridgeitem.dto;

import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.user.validation.EnumValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FridgeItemSaveRequest(
        @JsonProperty("name")
        @NotBlank(message = "식품명은 필수입니다.")
        @Size(max = 100, message = "식품명은 100자 이하여야 합니다.")
        String name,

        @JsonProperty("quantity")
        @NotBlank(message = "수량은 필수입니다.")
        @Size(max = 50, message = "수량은 50자 이하여야 합니다.")
        String quantity,

        @JsonProperty("storage_location")
        @NotNull(message = "보관 위치는 필수입니다.")
        @EnumValue(enumClass = StorageLocation.class, message = "보관 위치 값이 올바르지 않습니다.")
        String storageLocation,

        @JsonProperty("expiration_date")
        @NotNull(message = "유통기한은 필수입니다.")
        LocalDate expirationDate
) {
}
