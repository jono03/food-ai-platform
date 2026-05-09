package com.example.foodaiflatformserver.user.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @ElementCollection(targetClass = FavoriteCuisine.class)
    @CollectionTable(name = "user_preference_favorite_cuisines", joinColumns = @JoinColumn(name = "user_preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "favorite_cuisine", nullable = false)
    @OrderColumn(name = "sort_order")
    private List<FavoriteCuisine> favoriteCuisines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_preference", nullable = false)
    private DifficultyPreference difficultyPreference;

    @Column(name = "quick_meal_preferred", nullable = false)
    private boolean quickMealPreferred;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserPreference() {
    }

    public UserPreference(Long userId,
                          List<FavoriteCuisine> favoriteCuisines,
                          DifficultyPreference difficultyPreference,
                          boolean quickMealPreferred,
                          LocalDateTime updatedAt) {
        this.userId = userId;
        this.favoriteCuisines = new ArrayList<>(favoriteCuisines);
        this.difficultyPreference = difficultyPreference;
        this.quickMealPreferred = quickMealPreferred;
        this.updatedAt = updatedAt;
    }

    public void update(List<FavoriteCuisine> favoriteCuisines,
                       DifficultyPreference difficultyPreference,
                       boolean quickMealPreferred,
                       LocalDateTime updatedAt) {
        this.favoriteCuisines.clear();
        this.favoriteCuisines.addAll(favoriteCuisines);
        this.difficultyPreference = difficultyPreference;
        this.quickMealPreferred = quickMealPreferred;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public List<FavoriteCuisine> getFavoriteCuisines() {
        return List.copyOf(favoriteCuisines);
    }

    public DifficultyPreference getDifficultyPreference() {
        return difficultyPreference;
    }

    public boolean isQuickMealPreferred() {
        return quickMealPreferred;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
