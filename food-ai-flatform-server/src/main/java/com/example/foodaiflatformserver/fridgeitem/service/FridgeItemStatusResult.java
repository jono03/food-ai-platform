package com.example.foodaiflatformserver.fridgeitem.service;

import com.example.foodaiflatformserver.fridgeitem.entity.ItemStatus;

public record FridgeItemStatusResult(
        int dDay,
        String statusText,
        ItemStatus status
) {
}
