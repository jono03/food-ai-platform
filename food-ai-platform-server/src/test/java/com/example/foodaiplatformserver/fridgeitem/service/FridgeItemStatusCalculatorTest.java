package com.example.foodaiplatformserver.fridgeitem.service;

import com.example.foodaiplatformserver.fridgeitem.entity.ItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class FridgeItemStatusCalculatorTest {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-04-27T00:00:00Z"), ASIA_SEOUL);

    private final FridgeItemStatusCalculator calculator = new FridgeItemStatusCalculator(FIXED_CLOCK);

    @DisplayName("유통기한이 4일 이상 남았으면 OK와 D-day 텍스트를 반환한다")
    @Test
    void returnsOkForItemsWithEnoughDaysLeft() {
        FridgeItemStatusResult result = calculator.calculate(LocalDate.of(2026, 5, 2));

        assertThat(result.dDay()).isEqualTo(5);
        assertThat(result.status()).isEqualTo(ItemStatus.OK);
        assertThat(result.statusText()).isEqualTo("D-5");
    }

    @DisplayName("유통기한이 3일 이내면 WARNING 상태를 반환한다")
    @Test
    void returnsWarningForItemsExpiringSoon() {
        FridgeItemStatusResult result = calculator.calculate(LocalDate.of(2026, 4, 30));

        assertThat(result.dDay()).isEqualTo(3);
        assertThat(result.status()).isEqualTo(ItemStatus.WARNING);
        assertThat(result.statusText()).isEqualTo("D-3");
    }

    @DisplayName("오늘 만료되는 식품은 WARNING과 오늘 만료 텍스트를 반환한다")
    @Test
    void returnsTodayStatusTextForSameDayExpiration() {
        FridgeItemStatusResult result = calculator.calculate(LocalDate.of(2026, 4, 27));

        assertThat(result.dDay()).isZero();
        assertThat(result.status()).isEqualTo(ItemStatus.WARNING);
        assertThat(result.statusText()).isEqualTo("오늘 만료");
    }

    @DisplayName("유통기한이 지난 식품은 EXPIRED와 지난 일수 텍스트를 반환한다")
    @Test
    void returnsExpiredForPastExpiration() {
        FridgeItemStatusResult result = calculator.calculate(LocalDate.of(2026, 4, 25));

        assertThat(result.dDay()).isEqualTo(-2);
        assertThat(result.status()).isEqualTo(ItemStatus.EXPIRED);
        assertThat(result.statusText()).isEqualTo("2일 지남");
    }

    @DisplayName("기본 비즈니스 시계는 Asia/Seoul 타임존을 사용한다")
    @Test
    void usesAsiaSeoulForBusinessClock() {
        assertThat(FridgeItemStatusCalculator.BUSINESS_ZONE).isEqualTo(ASIA_SEOUL);
        assertThat(FridgeItemStatusCalculator.businessClock().getZone()).isEqualTo(ASIA_SEOUL);
    }
}
