package com.example.foodaiplatformserver.fridgeitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record FridgeItemSummaryResponse(
        @JsonProperty("total_count")
        int totalCount,

        @JsonProperty("expiring_soon_count")
        int expiringSoonCount,

        @JsonProperty("expired_count")
        int expiredCount,

        @JsonProperty("location_stats")
        Map<String, Integer> locationStats,

        @JsonProperty("expiring_soon_items")
        List<FridgeItemSummaryItemResponse> expiringSoonItems,

        @JsonProperty("expired_items")
        List<FridgeItemSummaryItemResponse> expiredItems
) {
}
