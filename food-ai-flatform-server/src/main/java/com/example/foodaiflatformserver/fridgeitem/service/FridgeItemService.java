package com.example.foodaiflatformserver.fridgeitem.service;

import com.example.foodaiflatformserver.auth.security.UserPrincipal;
import com.example.foodaiflatformserver.common.exception.ForbiddenException;
import com.example.foodaiflatformserver.common.exception.NotFoundException;
import com.example.foodaiflatformserver.fridgeitem.dto.FridgeItemResponse;
import com.example.foodaiflatformserver.fridgeitem.dto.FridgeItemSaveRequest;
import com.example.foodaiflatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiflatformserver.fridgeitem.entity.ItemStatus;
import com.example.foodaiflatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiflatformserver.fridgeitem.repository.FridgeItemRepository;
import com.example.foodaiflatformserver.user.entity.User;
import com.example.foodaiflatformserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeItemService {

    private final FridgeItemRepository fridgeItemRepository;
    private final UserRepository userRepository;
    private final FridgeItemStatusCalculator statusCalculator;

    public List<FridgeItemResponse> getItems(
            UserPrincipal principal,
            String keyword,
            StorageLocation storageLocation,
            ItemStatus status
    ) {
        String normalizedKeyword = keyword == null ? null : normalize(keyword).toLowerCase(Locale.ROOT);

        return fridgeItemRepository.findAllByUserIdOrderByExpirationDateAsc(principal.id())
                .stream()
                .filter(fridgeItem -> matchesKeyword(fridgeItem, normalizedKeyword))
                .map(fridgeItem -> FridgeItemResponse.from(fridgeItem, statusCalculator))
                .filter(response -> matchesStorageLocation(response, storageLocation))
                .filter(response -> matchesStatus(response, status))
                .toList();
    }

    @Transactional
    public FridgeItemResponse create(UserPrincipal principal, FridgeItemSaveRequest request) {
        User user = getUser(principal.id());

        FridgeItem fridgeItem = FridgeItem.builder()
                .user(user)
                .name(normalize(request.name()))
                .quantity(normalize(request.quantity()))
                .storageLocation(request.storageLocation())
                .registeredDate(LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE))
                .expirationDate(request.expirationDate())
                .build();

        FridgeItem savedItem = fridgeItemRepository.save(fridgeItem);
        return FridgeItemResponse.from(savedItem, statusCalculator);
    }

    @Transactional
    public FridgeItemResponse update(UserPrincipal principal, Long itemId, FridgeItemSaveRequest request) {
        FridgeItem fridgeItem = getOwnedFridgeItem(principal.id(), itemId);
        fridgeItem.update(
                normalize(request.name()),
                normalize(request.quantity()),
                request.storageLocation(),
                request.expirationDate()
        );

        return FridgeItemResponse.from(fridgeItem, statusCalculator);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long itemId) {
        FridgeItem fridgeItem = getOwnedFridgeItem(principal.id(), itemId);
        fridgeItemRepository.delete(fridgeItem);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private FridgeItem getOwnedFridgeItem(Long userId, Long itemId) {
        FridgeItem fridgeItem = fridgeItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("식재료를 찾을 수 없습니다."));

        if (!fridgeItem.getUser().getId().equals(userId)) {
            throw new ForbiddenException("다른 사용자의 식재료에는 접근할 수 없습니다.");
        }

        return fridgeItem;
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private boolean matchesKeyword(FridgeItem fridgeItem, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        return fridgeItem.getName().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean matchesStorageLocation(FridgeItemResponse response, StorageLocation storageLocation) {
        return storageLocation == null || response.storageLocation().equals(storageLocation.name());
    }

    private boolean matchesStatus(FridgeItemResponse response, ItemStatus status) {
        return status == null || response.status().equals(status.name());
    }
}
