package com.example.foodaiplatformserver.fridgeitem.controller;

import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemResponse;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSaveRequest;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSummaryResponse;
import com.example.foodaiplatformserver.fridgeitem.entity.ItemStatus;
import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.fridgeitem.service.FridgeItemService;
import com.example.foodaiplatformserver.user.validation.EnumValue;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/fridge-items")
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;

    public FridgeItemController(FridgeItemService fridgeItemService) {
        this.fridgeItemService = fridgeItemService;
    }

    @GetMapping("/summary")
    public FridgeItemSummaryResponse getSummary() {
        return fridgeItemService.getSummary();
    }

    @GetMapping
    public List<FridgeItemResponse> getItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "storage_location", required = false)
            @EnumValue(enumClass = StorageLocation.class, message = "보관 위치 값이 올바르지 않습니다.") String storageLocation,
            @RequestParam(required = false)
            @EnumValue(enumClass = ItemStatus.class, message = "식품 상태 값이 올바르지 않습니다.") String status
    ) {
        return fridgeItemService.getItems(keyword, storageLocation, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FridgeItemResponse create(@Valid @RequestBody FridgeItemSaveRequest request) {
        return fridgeItemService.create(request);
    }

    @PutMapping("/{itemId}")
    public FridgeItemResponse update(@PathVariable Long itemId,
                                     @Valid @RequestBody FridgeItemSaveRequest request) {
        return fridgeItemService.update(itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long itemId) {
        fridgeItemService.delete(itemId);
    }
}
