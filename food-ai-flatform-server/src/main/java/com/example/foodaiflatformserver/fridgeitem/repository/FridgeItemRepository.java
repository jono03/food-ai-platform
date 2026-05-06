package com.example.foodaiflatformserver.fridgeitem.repository;

import com.example.foodaiflatformserver.fridgeitem.entity.FridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {

    List<FridgeItem> findAllByUserIdOrderByExpirationDateAsc(Long userId);
}
