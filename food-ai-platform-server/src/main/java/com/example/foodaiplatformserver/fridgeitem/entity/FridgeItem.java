package com.example.foodaiplatformserver.fridgeitem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(
        name = "fridge_items",
        indexes = {
                @Index(name = "idx_fridge_items_user_id", columnList = "user_id")
        }
)
public class FridgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "quantity", nullable = false, length = 50)
    private String quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_location", nullable = false, length = 20)
    private StorageLocation storageLocation;

    @Column(name = "registered_date", nullable = false)
    private LocalDate registeredDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    protected FridgeItem() {
    }

    public FridgeItem(Long userId,
                      String name,
                      String quantity,
                      StorageLocation storageLocation,
                      LocalDate registeredDate,
                      LocalDate expirationDate) {
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.registeredDate = registeredDate;
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public StorageLocation getStorageLocation() {
        return storageLocation;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}
