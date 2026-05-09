package com.example.foodaiplatformserver.userrecipe.controller;

import com.example.foodaiplatformserver.userrecipe.dto.UserRecipeCreateRequest;
import com.example.foodaiplatformserver.userrecipe.dto.UserRecipeResponse;
import com.example.foodaiplatformserver.userrecipe.service.UserRecipeService;
import com.example.foodaiplatformserver.common.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/user-recipes")
@Tag(name = "사용자 레시피 히스토리", description = "사용자 레시피 저장 및 조회 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class UserRecipeController {

    private final UserRecipeService userRecipeService;

    public UserRecipeController(UserRecipeService userRecipeService) {
        this.userRecipeService = userRecipeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "레시피 히스토리 저장", description = "사용자가 선택한 레시피를 저장합니다.")
    public UserRecipeResponse create(@Valid @RequestBody UserRecipeCreateRequest request) {
        return userRecipeService.create(request);
    }

    @GetMapping
    @Operation(summary = "내 레시피 히스토리 조회", description = "현재 사용자의 레시피 저장 이력을 조회합니다.")
    public List<UserRecipeResponse> getMyHistories(
            @Parameter(description = "조회 개수 제한", example = "10")
            @RequestParam(name = "limit", required = false) @Min(value = 1, message = "limit는 1 이상이어야 합니다.") Integer limit
    ) {
        return userRecipeService.getMyHistories(limit);
    }
}
