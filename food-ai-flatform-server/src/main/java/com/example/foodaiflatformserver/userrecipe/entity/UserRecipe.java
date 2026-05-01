package com.example.foodaiflatformserver.userrecipe.entity;

import com.example.foodaiflatformserver.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "user_recipes",
        indexes = {
                @Index(name = "idx_user_recipes_user_id", columnList = "user_id"),
                @Index(name = "idx_user_recipes_selected_at", columnList = "selected_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "recipe_name", nullable = false, length = 100)
    private String recipeName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    @Builder
    public UserRecipe(User user, Long recipeId, String recipeName, String category, LocalDateTime selectedAt) {
        this.user = user;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.category = category;
        this.selectedAt = selectedAt;
    }

    @PrePersist
    void prePersist() {
        if (selectedAt == null) {
            selectedAt = LocalDateTime.now();
        }
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
