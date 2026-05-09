package com.example.foodaiplatformserver.fridgeitem.controller;

import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemResponse;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSaveRequest;
import com.example.foodaiplatformserver.fridgeitem.dto.FridgeItemSummaryResponse;
import com.example.foodaiplatformserver.fridgeitem.entity.ItemStatus;
import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.fridgeitem.service.FridgeItemService;
import com.example.foodaiplatformserver.common.config.OpenApiConfig;
import com.example.foodaiplatformserver.user.validation.EnumValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "냉장고 식재료", description = "냉장고 식재료 조회, 등록, 수정, 삭제 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;

    public FridgeItemController(FridgeItemService fridgeItemService) {
        this.fridgeItemService = fridgeItemService;
    }

    @GetMapping("/summary")
    @Operation(summary = "식재료 요약 조회", description = "냉장고 식재료 개수와 상태 요약을 조회합니다.")
    public FridgeItemSummaryResponse getSummary() {
        return fridgeItemService.getSummary();
    }

    @GetMapping
    @Operation(summary = "식재료 목록 조회", description = "키워드, 보관 위치, 식품 상태로 식재료를 조회합니다.")
    public List<FridgeItemResponse> getItems(
            @Parameter(description = "식재료 이름 검색 키워드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "보관 위치 필터", example = "REFRIGERATED")
            @RequestParam(name = "storage_location", required = false)
            @EnumValue(enumClass = StorageLocation.class, message = "보관 위치 값이 올바르지 않습니다.") String storageLocation,
            @Parameter(description = "식품 상태 필터", example = "SAFE")
            @RequestParam(required = false)
            @EnumValue(enumClass = ItemStatus.class, message = "식품 상태 값이 올바르지 않습니다.") String status
    ) {
        return fridgeItemService.getItems(keyword, storageLocation, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "식재료 등록", description = "새 식재료를 냉장고에 등록합니다.")
    public FridgeItemResponse create(@Valid @RequestBody FridgeItemSaveRequest request) {
        return fridgeItemService.create(request);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "식재료 수정", description = "기존 식재료 정보를 수정합니다.")
    public FridgeItemResponse update(@PathVariable Long itemId,
                                     @Valid @RequestBody FridgeItemSaveRequest request) {
        return fridgeItemService.update(itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "식재료 삭제", description = "식재료를 삭제합니다.")
    public void delete(@PathVariable Long itemId) {
        fridgeItemService.delete(itemId);
    }
}
