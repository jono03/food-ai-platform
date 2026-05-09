package com.example.foodaiplatformserver.fridgeitem.service;

import com.example.foodaiplatformserver.common.exception.ForbiddenException;
import com.example.foodaiplatformserver.common.exception.NotFoundException;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemResponse;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSaveRequest;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSummaryItemResponse;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSummaryResponse;
import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.fridgeitem.entity.ItemStatus;
import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.fridgeitem.repository.FridgeItemRepository;
import com.example.foodaiplatformserver.user.service.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class FridgeItemService {

    private final FridgeItemRepository fridgeItemRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FridgeItemStatusCalculator statusCalculator;

    public FridgeItemService(FridgeItemRepository fridgeItemRepository,
                             CurrentUserProvider currentUserProvider) {
        this.fridgeItemRepository = fridgeItemRepository;
        this.currentUserProvider = currentUserProvider;
        this.statusCalculator = new FridgeItemStatusCalculator();
    }

    public FridgeItemSummaryResponse getSummary() {
        List<FridgeItemResponse> responses = findCurrentUserItemResponses();
        Map<String, Integer> locationStats = initializeLocationStats();

        responses.forEach(item -> locationStats.compute(item.storageLocation(), (key, value) -> value + 1));

        List<FridgeItemSummaryItemResponse> expiringSoonItems = responses.stream()
                .filter(item -> "WARNING".equals(item.status()))
                .map(this::toSummaryItem)
                .toList();

        List<FridgeItemSummaryItemResponse> expiredItems = responses.stream()
                .filter(item -> "EXPIRED".equals(item.status()))
                .map(this::toSummaryItem)
                .toList();

        return new FridgeItemSummaryResponse(
                responses.size(),
                expiringSoonItems.size(),
                expiredItems.size(),
                locationStats,
                expiringSoonItems,
                expiredItems
        );
    }

    public List<FridgeItemResponse> getItems(String keyword,
                                             String storageLocation,
                                             String status) {
        String normalizedKeyword = keyword == null ? null : normalize(keyword).toLowerCase(Locale.ROOT);

        return findCurrentUserItemResponses().stream()
                .filter(item -> matchesKeyword(item, normalizedKeyword))
                .filter(item -> matchesStorageLocation(item, storageLocation))
                .filter(item -> matchesStatus(item, status))
                .toList();
    }

    @Transactional
    public FridgeItemResponse create(FridgeItemSaveRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        FridgeItem savedItem = fridgeItemRepository.save(new FridgeItem(
                userId,
                normalize(request.name()),
                normalize(request.quantity()),
                StorageLocation.valueOf(request.storageLocation()),
                LocalDate.now(FridgeItemStatusCalculator.BUSINESS_ZONE),
                request.expirationDate()
        ));

        return FridgeItemResponse.from(savedItem, statusCalculator);
    }

    @Transactional
    public FridgeItemResponse update(Long itemId, FridgeItemSaveRequest request) {
        FridgeItem fridgeItem = getOwnedFridgeItem(itemId);
        fridgeItem.update(
                normalize(request.name()),
                normalize(request.quantity()),
                StorageLocation.valueOf(request.storageLocation()),
                request.expirationDate()
        );

        return FridgeItemResponse.from(fridgeItem, statusCalculator);
    }

    @Transactional
    public void delete(Long itemId) {
        fridgeItemRepository.delete(getOwnedFridgeItem(itemId));
    }

    private List<FridgeItemResponse> findCurrentUserItemResponses() {
        return fridgeItemRepository.findAllByUserIdOrderByExpirationDateAscIdAsc(currentUserProvider.getCurrentUserId())
                .stream()
                .map(item -> FridgeItemResponse.from(item, statusCalculator))
                .toList();
    }

    private FridgeItem getOwnedFridgeItem(Long itemId) {
        Long userId = currentUserProvider.getCurrentUserId();
        FridgeItem fridgeItem = fridgeItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("식재료를 찾을 수 없습니다."));

        if (!fridgeItem.getUserId().equals(userId)) {
            throw new ForbiddenException("다른 사용자의 식재료에는 접근할 수 없습니다.");
        }

        return fridgeItem;
    }

    private Map<String, Integer> initializeLocationStats() {
        Map<String, Integer> locationStats = new LinkedHashMap<>();
        Arrays.stream(StorageLocation.values())
                .map(Enum::name)
                .forEach(location -> locationStats.put(location, 0));
        return locationStats;
    }

    private FridgeItemSummaryItemResponse toSummaryItem(FridgeItemResponse response) {
        return new FridgeItemSummaryItemResponse(
                response.itemId(),
                response.name(),
                response.dDay(),
                response.statusText()
        );
    }

    private boolean matchesKeyword(FridgeItemResponse item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        return item.name().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean matchesStorageLocation(FridgeItemResponse item, String storageLocation) {
        return storageLocation == null || storageLocation.isBlank() || item.storageLocation().equals(storageLocation);
    }

    private boolean matchesStatus(FridgeItemResponse item, String status) {
        return status == null || status.isBlank() || item.status().equals(status);
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}
