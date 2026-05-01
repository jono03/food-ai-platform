package com.example.foodaiflatformserver.fridgeitem.entity;

import com.example.foodaiflatformserver.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "fridge_items",
        indexes = {
                @Index(name = "idx_fridge_items_user_id", columnList = "user_id"),
                @Index(name = "idx_fridge_items_expiration_date", columnList = "expiration_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_location", nullable = false, length = 20)
    private StorageLocation storageLocation;

    @Column(name = "registered_date", nullable = false)
    private LocalDate registeredDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Builder
    public FridgeItem(User user, String name, String quantity, StorageLocation storageLocation, LocalDate registeredDate,
                      LocalDate expirationDate) {
        this.user = user;
        this.name = name;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.registeredDate = registeredDate;
        this.expirationDate = expirationDate;
    }

    @PrePersist
    void prePersist() {
        if (registeredDate == null) {
            registeredDate = LocalDate.now();
        }
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
