package com.example.foodaiplatformserver.fridgeitem.service;

import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.fridgeitem.repository.FridgeItemRepository;
import com.example.foodaiplatformserver.user.service.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CurrentFridgeItemFinder {

    private final FridgeItemRepository fridgeItemRepository;
    private final CurrentUserProvider currentUserProvider;

    public CurrentFridgeItemFinder(FridgeItemRepository fridgeItemRepository,
                                   CurrentUserProvider currentUserProvider) {
        this.fridgeItemRepository = fridgeItemRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<FridgeItem> findCurrentUserItems() {
        return fridgeItemRepository.findAllByUserId(currentUserProvider.getCurrentUserId());
    }
}
