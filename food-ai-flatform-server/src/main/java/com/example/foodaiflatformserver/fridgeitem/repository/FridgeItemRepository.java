package com.example.foodaiflatformserver.fridgeitem.repository;

import com.example.foodaiflatformserver.fridgeitem.entity.FridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {
}
