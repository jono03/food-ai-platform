package com.example.foodaiplatformserver.userrecipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRecipeCreateRequest(
        @JsonProperty("recipe_id")
        @NotNull(message = "레시피 ID는 필수입니다.")
        Long recipeId,

        @JsonProperty("recipe_name")
        @NotBlank(message = "레시피 이름은 필수입니다.")
        String recipeName,

        @JsonProperty("category")
        @NotBlank(message = "카테고리는 필수입니다.")
        String category
) {
}
