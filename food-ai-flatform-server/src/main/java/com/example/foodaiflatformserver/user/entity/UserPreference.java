package com.example.foodaiflatformserver.user.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Entity
@Table(
        name = "user_preferences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_preferences_user_id", columnNames = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_preference_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(
            name = "user_preference_favorite_cuisines",
            joinColumns = @JoinColumn(name = "user_preference_id", nullable = false)
    )
    @Column(name = "favorite_cuisine", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Set<FavoriteCuisine> favoriteCuisines = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_preference", nullable = false, length = 20)
    private DifficultyPreference difficultyPreference;

    @Column(name = "quick_meal_preferred", nullable = false)
    private boolean quickMealPreferred;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserPreference(Set<FavoriteCuisine> favoriteCuisines, DifficultyPreference difficultyPreference,
                          boolean quickMealPreferred) {
        if (favoriteCuisines != null) {
            this.favoriteCuisines = new LinkedHashSet<>(favoriteCuisines);
        }
        this.difficultyPreference = difficultyPreference;
        this.quickMealPreferred = quickMealPreferred;
    }

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
