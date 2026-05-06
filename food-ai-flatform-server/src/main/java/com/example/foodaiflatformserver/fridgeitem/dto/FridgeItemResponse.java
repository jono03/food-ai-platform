package com.example.foodaiflatformserver.fridgeitem.dto;

import com.example.foodaiflatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiflatformserver.fridgeitem.service.FridgeItemStatusCalculator;
import com.example.foodaiflatformserver.fridgeitem.service.FridgeItemStatusResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record FridgeItemResponse(
        @JsonProperty("item_id")
        Long itemId,

        @JsonProperty("name")
        String name,

        @JsonProperty("quantity")
        String quantity,

        @JsonProperty("storage_location")
        String storageLocation,

        @JsonProperty("registered_date")
        LocalDate registeredDate,

        @JsonProperty("expiration_date")
        LocalDate expirationDate,

        @JsonProperty("status")
        String status,

        @JsonProperty("d_day")
        int dDay,

        @JsonProperty("status_text")
        String statusText
) {

    public static FridgeItemResponse from(FridgeItem fridgeItem, FridgeItemStatusCalculator statusCalculator) {
        FridgeItemStatusResult statusResult = statusCalculator.calculate(fridgeItem.getExpirationDate());

        return new FridgeItemResponse(
                fridgeItem.getId(),
                fridgeItem.getName(),
                fridgeItem.getQuantity(),
                fridgeItem.getStorageLocation().name(),
                fridgeItem.getRegisteredDate(),
                fridgeItem.getExpirationDate(),
                statusResult.status().name(),
                statusResult.dDay(),
                statusResult.statusText()
        );
    }
}
