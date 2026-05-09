package com.example.foodaiplatformserver.fridgeitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FridgeItemSummaryItemResponse(
        @JsonProperty("item_id")
        Long itemId,

        @JsonProperty("name")
        String name,

        @JsonProperty("d_day")
        int dDay,

        @JsonProperty("status_text")
        String statusText
) {
}
