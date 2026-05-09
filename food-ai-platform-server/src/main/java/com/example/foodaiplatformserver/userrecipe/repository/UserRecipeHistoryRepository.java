package com.example.foodaiplatformserver.userrecipe.repository;

import com.example.foodaiplatformserver.userrecipe.entity.UserRecipeHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRecipeHistoryRepository extends JpaRepository<UserRecipeHistory, Long> {

    List<UserRecipeHistory> findByUserIdOrderBySelectedAtDescIdDesc(Long userId, Pageable pageable);
}
