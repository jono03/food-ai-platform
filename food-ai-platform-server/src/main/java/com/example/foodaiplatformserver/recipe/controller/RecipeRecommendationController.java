package com.example.foodaiplatformserver.recipe.controller;

import com.example.foodaiplatformserver.recipe.dto.RecipeRecommendationResponse;
import com.example.foodaiplatformserver.recipe.service.RecipeRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes")
public class RecipeRecommendationController {

    private final RecipeRecommendationService recipeRecommendationService;

    public RecipeRecommendationController(RecipeRecommendationService recipeRecommendationService) {
        this.recipeRecommendationService = recipeRecommendationService;
    }

    @GetMapping("/recommendations")
    public RecipeRecommendationResponse getRecommendations() {
        return recipeRecommendationService.getRecommendations();
    }
}
