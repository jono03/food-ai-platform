package com.example.foodaiflatformserver.fridgeitem.controller;

import com.example.foodaiflatformserver.auth.security.UserPrincipal;
import com.example.foodaiflatformserver.fridgeitem.dto.FridgeItemResponse;
import com.example.foodaiflatformserver.fridgeitem.dto.FridgeItemSaveRequest;
import com.example.foodaiflatformserver.fridgeitem.service.FridgeItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fridge-items")
@RequiredArgsConstructor
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FridgeItemResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FridgeItemSaveRequest request
    ) {
        return fridgeItemService.create(principal, request);
    }

    @PutMapping("/{itemId}")
    public FridgeItemResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody FridgeItemSaveRequest request
    ) {
        return fridgeItemService.update(principal, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId
    ) {
        fridgeItemService.delete(principal, itemId);
    }
}
