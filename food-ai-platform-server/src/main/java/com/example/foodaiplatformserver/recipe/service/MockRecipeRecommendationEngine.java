package com.example.foodaiplatformserver.recipe.service;

import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.fridgeitem.service.FridgeItemStatusCalculator;
import com.example.foodaiplatformserver.user.entity.DifficultyPreference;
import com.example.foodaiplatformserver.user.entity.FavoriteCuisine;
import com.example.foodaiplatformserver.user.entity.UserPreference;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("mockRecipeRecommendationEngine")
public class MockRecipeRecommendationEngine implements RecipeRecommendationEngine {

    private static final int FEW_INGREDIENTS_THRESHOLD = 2;

    private final FridgeItemStatusCalculator fridgeItemStatusCalculator = new FridgeItemStatusCalculator();

    @Override
    public RecipeRecommendationResult recommend(RecipeRecommendationCriteria criteria) {
        Set<String> availableIngredients = criteria.fridgeItems().stream()
                .map(FridgeItem::getName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        Set<String> expiringIngredients = criteria.fridgeItems().stream()
                .filter(item -> fridgeItemStatusCalculator.calculateDDay(item.getExpirationDate()) <= 3)
                .map(FridgeItem::getName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        Optional<UserPreference> userPreference = criteria.userPreference();

        List<RecommendedRecipe> matchedRecipes = recipeCatalog().stream()
                .filter(recipe -> matchesPreference(recipe, userPreference))
                .map(recipe -> evaluateRecipe(recipe, availableIngredients, expiringIngredients))
                .filter(EvaluatedRecipe::isRecommendable)
                .sorted(Comparator
                        .comparingInt(EvaluatedRecipe::expiringIngredientCount).reversed()
                        .thenComparingInt(EvaluatedRecipe::missingIngredientCount)
                        .thenComparing(evaluatedRecipe -> evaluatedRecipe.recipe().recipeId()))
                .map(EvaluatedRecipe::recipe)
                .toList();

        List<RecommendedRecipe> availableNow = matchedRecipes.stream()
                .filter(recipe -> recipe.missingIngredients().isEmpty())
                .toList();

        List<RecommendedRecipe> needFewIngredients = matchedRecipes.stream()
                .filter(recipe -> !recipe.missingIngredients().isEmpty())
                .toList();

        return new RecipeRecommendationResult(availableNow, needFewIngredients);
    }

    private boolean matchesPreference(RecipeCatalogRecipe recipe, Optional<UserPreference> userPreference) {
        if (userPreference.isEmpty()) {
            return true;
        }

        UserPreference preference = userPreference.get();
        boolean cuisineMatched = preference.getFavoriteCuisines().isEmpty()
                || preference.getFavoriteCuisines().contains(recipe.favoriteCuisine());
        boolean difficultyMatched = preference.getDifficultyPreference() == recipe.difficultyPreference();
        boolean quickMealMatched = !preference.isQuickMealPreferred() || recipe.quickMeal();

        return cuisineMatched && difficultyMatched && quickMealMatched;
    }

    private EvaluatedRecipe evaluateRecipe(RecipeCatalogRecipe recipe,
                                           Set<String> availableIngredients,
                                           Set<String> expiringIngredients) {
        List<String> missingIngredients = recipe.allIngredients().stream()
                .filter(ingredient -> !availableIngredients.contains(ingredient))
                .toList();

        List<String> expiringIngredientsUsed = recipe.allIngredients().stream()
                .filter(expiringIngredients::contains)
                .toList();

        boolean recommendable = missingIngredients.size() <= FEW_INGREDIENTS_THRESHOLD
                && !expiringIngredientsUsed.isEmpty();

        RecommendedRecipe recommendedRecipe = new RecommendedRecipe(
                recipe.recipeId(),
                recipe.recipeName(),
                recipe.category(),
                expiringIngredientsUsed,
                recipe.allIngredients(),
                missingIngredients,
                recipe.instructions()
        );

        return new EvaluatedRecipe(recommendedRecipe, recommendable);
    }

    private List<RecipeCatalogRecipe> recipeCatalog() {
        return List.of(
                new RecipeCatalogRecipe(
                        101L,
                        "대파 달걀 볶음",
                        "한식",
                        FavoriteCuisine.KOREAN,
                        DifficultyPreference.EASY,
                        true,
                        List.of("대파", "달걀", "양파", "우유"),
                        List.of(
                                "대파를 송송 썬다.",
                                "양파도 채 썬다.",
                                "팬에 기름을 두르고 대파와 양파를 볶는다.",
                                "양파가 투명해지면 달걀을 풀어 넣고 섞는다.",
                                "우유를 조금 넣고 잘 섞어서 스크램블 에그처럼 만든다.",
                                "완성된 대파 달걀 볶음을 접시에 담아낸다."
                        )
                ),
                new RecipeCatalogRecipe(
                        102L,
                        "소고기 장조림",
                        "한식",
                        FavoriteCuisine.KOREAN,
                        DifficultyPreference.EASY,
                        false,
                        List.of("소고기", "양파", "대파", "간장", "설탕", "다진 마늘"),
                        List.of(
                                "소고기를 핏물을 빼고 삶는다.",
                                "간장, 설탕 등으로 양념장을 만든다.",
                                "삶은 소고기와 양념장을 넣고 조린다."
                        )
                ),
                new RecipeCatalogRecipe(
                        201L,
                        "계란 간장밥",
                        "한식",
                        FavoriteCuisine.KOREAN,
                        DifficultyPreference.NORMAL,
                        true,
                        List.of("달걀", "간장", "밥"),
                        List.of(
                                "따뜻한 밥을 그릇에 담는다.",
                                "계란 프라이를 올린다.",
                                "간장을 넣고 비벼 먹는다."
                        )
                ),
                new RecipeCatalogRecipe(
                        301L,
                        "연어 스테이크",
                        "양식",
                        FavoriteCuisine.WESTERN,
                        DifficultyPreference.NORMAL,
                        false,
                        List.of("연어", "버터", "레몬", "허브"),
                        List.of(
                                "연어에 소금과 후추로 밑간한다.",
                                "팬에 버터를 녹여 연어를 굽는다.",
                                "레몬과 허브를 곁들여 마무리한다."
                        )
                )
        );
    }

    private record RecipeCatalogRecipe(
            Long recipeId,
            String recipeName,
            String category,
            FavoriteCuisine favoriteCuisine,
            DifficultyPreference difficultyPreference,
            boolean quickMeal,
            List<String> allIngredients,
            List<String> instructions
    ) {
    }

    private record EvaluatedRecipe(
            RecommendedRecipe recipe,
            boolean recommendable
    ) {
        int expiringIngredientCount() {
            return recipe.expiringIngredientsUsed().size();
        }

        int missingIngredientCount() {
            return recipe.missingIngredients().size();
        }

        boolean isRecommendable() {
            return recommendable;
        }
    }
}
