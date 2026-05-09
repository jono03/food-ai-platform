package com.example.foodaiplatformserver.domain;

import com.example.foodaiplatformserver.fridgeitem.entity.ItemStatus;
import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.user.entity.DifficultyPreference;
import com.example.foodaiplatformserver.user.entity.FavoriteCuisine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEnumConsistencyTest {

    @DisplayName("보관 위치 enum은 API 스펙 값과 일치한다")
    @Test
    void storageLocationMatchesApiSpec() {
        assertThat(StorageLocation.values())
                .extracting(Enum::name)
                .containsExactly("REFRIGERATED", "FROZEN", "ROOM_TEMP");
    }

    @DisplayName("식품 상태 enum은 API 스펙 값과 일치한다")
    @Test
    void itemStatusMatchesApiSpec() {
        assertThat(ItemStatus.values())
                .extracting(Enum::name)
                .containsExactly("OK", "WARNING", "EXPIRED");
    }

    @DisplayName("선호 요리 enum은 API 스펙 값과 일치한다")
    @Test
    void favoriteCuisineMatchesApiSpec() {
        assertThat(FavoriteCuisine.values())
                .extracting(Enum::name)
                .containsExactly("KOREAN", "JAPANESE", "WESTERN", "CHINESE");
    }

    @DisplayName("난이도 enum은 API 스펙 값과 일치한다")
    @Test
    void difficultyPreferenceMatchesApiSpec() {
        assertThat(DifficultyPreference.values())
                .extracting(Enum::name)
                .containsExactly("EASY", "NORMAL", "HARD");
    }
}
