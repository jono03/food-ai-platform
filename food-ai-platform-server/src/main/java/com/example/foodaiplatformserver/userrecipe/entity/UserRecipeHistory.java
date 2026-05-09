package com.example.foodaiplatformserver.userrecipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_recipes",
        indexes = {
                @Index(name = "idx_user_recipes_user_selected_at", columnList = "user_id, selected_at")
        }
)
public class UserRecipeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "recipe_name", nullable = false, length = 255)
    private String recipeName;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    protected UserRecipeHistory() {
    }

    public UserRecipeHistory(Long userId,
                             Long recipeId,
                             String recipeName,
                             String category,
                             LocalDateTime selectedAt) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.category = category;
        this.selectedAt = selectedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }
}
