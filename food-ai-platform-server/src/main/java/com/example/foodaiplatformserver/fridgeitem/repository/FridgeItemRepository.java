package com.example.foodaiplatformserver.fridgeitem.repository;

import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {

    List<FridgeItem> findAllByUserId(Long userId);

    List<FridgeItem> findAllByUserIdOrderByExpirationDateAscIdAsc(Long userId);
}
