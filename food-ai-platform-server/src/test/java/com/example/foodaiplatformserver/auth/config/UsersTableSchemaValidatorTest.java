package com.example.foodaiplatformserver.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UsersTableSchemaValidatorTest {

    @DisplayName("users 테이블이 id PK를 쓰면 현재 인증 스키마로 판단한다")
    @Test
    void acceptsCurrentUsersSchema() {
        Optional<String> validationError = UsersTableSchemaValidator.validateUsersTableSchema(
                Set.of("id", "username", "email", "password"),
                Set.of("id")
        );

        assertThat(validationError).isEmpty();
    }

    @DisplayName("users 테이블에 id 컬럼이 없으면 오류를 반환한다")
    @Test
    void returnsErrorWhenIdColumnMissing() {
        Optional<String> validationError = UsersTableSchemaValidator.validateUsersTableSchema(
                Set.of("user_id", "username", "email", "password"),
                Set.of("user_id")
        );

        assertThat(validationError).isPresent();
        assertThat(validationError.get()).contains("id 컬럼이 없습니다");
    }

    @DisplayName("id 컬럼은 있지만 PK가 아니면 오류를 반환한다")
    @Test
    void returnsErrorWhenIdIsNotPrimaryKey() {
        Optional<String> validationError = UsersTableSchemaValidator.validateUsersTableSchema(
                Set.of("id", "username", "email", "password"),
                Set.of("email")
        );

        assertThat(validationError).isPresent();
        assertThat(validationError.get()).contains("PK가 id가 아닙니다");
    }
}
