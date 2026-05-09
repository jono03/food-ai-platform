package com.example.foodaiplatformserver.auth.repository;

import com.example.foodaiplatformserver.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    Optional<UserAccount> findByEmail(String email);
}
