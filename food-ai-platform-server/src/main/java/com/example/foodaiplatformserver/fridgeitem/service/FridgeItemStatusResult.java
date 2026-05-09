package com.example.foodaiplatformserver.fridgeitem.service;

import com.example.foodaiplatformserver.fridgeitem.entity.ItemStatus;

public record FridgeItemStatusResult(
        int dDay,
        String statusText,
        ItemStatus status
) {
}
