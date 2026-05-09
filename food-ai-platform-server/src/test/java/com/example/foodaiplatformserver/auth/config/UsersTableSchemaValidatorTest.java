package com.example.foodaiplatformserver.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UsersTableSchemaValidatorTest {

    @DisplayName("users 테이블이 예전 id PK를 쓰면 명확한 스키마 오류를 반환한다")
    @Test
    void returnsHelpfulMessageForLegacyPrimaryKey() {
        Optional<String> validationError = UsersTableSchemaValidator.validateUsersTableSchema(
                Set.of("id", "username", "email", "password"),
                Set.of("id")
        );

        assertThat(validationError).isPresent();
        assertThat(validationError.get()).contains("users.user_id PK");
    }

    @DisplayName("users 테이블에 user_id PK가 있으면 정상 스키마로 판단한다")
    @Test
    void acceptsExpectedUsersSchema() {
        Optional<String> validationError = UsersTableSchemaValidator.validateUsersTableSchema(
                Set.of("user_id", "username", "email", "password"),
                Set.of("user_id")
        );

        assertThat(validationError).isEmpty();
    }

    @DisplayName("user_id 컬럼은 있지만 PK가 아니면 오류를 반환한다")
    @Test
    void returnsErrorWhenUserIdIsNotPrimaryKey() {
        Optional<String> validationError = UsersTableSchemaValidator.validateUsersTableSchema(
                Set.of("user_id", "username", "email", "password"),
                Set.of("email")
        );

        assertThat(validationError).isPresent();
        assertThat(validationError.get()).contains("PK가 user_id가 아닙니다");
    }
}
