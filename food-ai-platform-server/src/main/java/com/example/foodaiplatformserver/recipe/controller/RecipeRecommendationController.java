package com.example.foodaiplatformserver.recipe.controller;

import com.example.foodaiplatformserver.recipe.dto.RecipeRecommendationResponse;
import com.example.foodaiplatformserver.recipe.service.RecipeRecommendationService;
import com.example.foodaiplatformserver.common.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes")
@Tag(name = "레시피 추천", description = "사용자 맞춤 레시피 추천 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class RecipeRecommendationController {

    private final RecipeRecommendationService recipeRecommendationService;

    public RecipeRecommendationController(RecipeRecommendationService recipeRecommendationService) {
        this.recipeRecommendationService = recipeRecommendationService;
    }

    @GetMapping("/recommendations")
    @Operation(summary = "추천 레시피 조회", description = "현재 사용자 기준 추천 레시피 목록을 조회합니다.")
    public RecipeRecommendationResponse getRecommendations() {
        return recipeRecommendationService.getRecommendations();
    }
}
