package com.example.foodaiflatformserver.fridgeitem.service;

import com.example.foodaiflatformserver.fridgeitem.entity.ItemStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class FridgeItemStatusCalculator {

    private final Clock clock;

    public FridgeItemStatusCalculator() {
        this(Clock.systemDefaultZone());
    }

    public FridgeItemStatusCalculator(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public FridgeItemStatusResult calculate(LocalDate expirationDate) {
        int dDay = calculateDDay(expirationDate);
        ItemStatus status = toStatus(dDay);
        String statusText = toStatusText(dDay);

        return new FridgeItemStatusResult(dDay, statusText, status);
    }

    public int calculateDDay(LocalDate expirationDate) {
        LocalDate today = LocalDate.now(clock);
        return Math.toIntExact(ChronoUnit.DAYS.between(today, requireExpirationDate(expirationDate)));
    }

    public ItemStatus calculateStatus(LocalDate expirationDate) {
        return toStatus(calculateDDay(expirationDate));
    }

    public String calculateStatusText(LocalDate expirationDate) {
        return toStatusText(calculateDDay(expirationDate));
    }

    private LocalDate requireExpirationDate(LocalDate expirationDate) {
        return Objects.requireNonNull(expirationDate, "expirationDate must not be null");
    }

    private ItemStatus toStatus(int dDay) {
        if (dDay < 0) {
            return ItemStatus.EXPIRED;
        }
        if (dDay <= 3) {
            return ItemStatus.WARNING;
        }
        return ItemStatus.OK;
    }

    private String toStatusText(int dDay) {
        if (dDay < 0) {
            return Math.abs(dDay) + "일 지남";
        }
        if (dDay == 0) {
            return "오늘 만료";
        }
        return "D-" + dDay;
    }
}
