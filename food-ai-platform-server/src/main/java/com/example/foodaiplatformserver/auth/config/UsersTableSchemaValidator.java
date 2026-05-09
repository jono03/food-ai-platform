package com.example.foodaiplatformserver.auth.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class UsersTableSchemaValidator implements ApplicationRunner {

    private static final String USERS_TABLE = "users";

    private final DataSource dataSource;

    public UsersTableSchemaValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Optional<String> validationError = inspectUsersTable(metaData, connection.getCatalog(), connection.getSchema());
            validationError.ifPresent(message -> {
                throw new IllegalStateException(message);
            });
        } catch (SQLException exception) {
            throw new IllegalStateException("users 테이블 스키마 검증 중 오류가 발생했습니다.", exception);
        }
    }

    Optional<String> inspectUsersTable(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        if (!tableExists(metaData, catalog, schema, USERS_TABLE)) {
            return Optional.empty();
        }

        Set<String> columns = readColumns(metaData, catalog, schema, USERS_TABLE);
        Set<String> primaryKeys = readPrimaryKeys(metaData, catalog, schema, USERS_TABLE);
        return validateUsersTableSchema(columns, primaryKeys);
    }

    static Optional<String> validateUsersTableSchema(Set<String> columns, Set<String> primaryKeys) {
        Set<String> normalizedColumns = normalize(columns);
        Set<String> normalizedPrimaryKeys = normalize(primaryKeys);

        if (normalizedPrimaryKeys.contains("id") && !normalizedPrimaryKeys.contains("user_id")) {
            return Optional.of("""
                    레거시 users 테이블 스키마가 감지되었습니다. 현재 애플리케이션은 users.user_id PK를 기대하지만, DB에는 기존 id PK가 남아 있습니다.
                    기존 users 테이블을 정리한 뒤 서버를 다시 띄워 엔티티 기준으로 재생성해주세요.
                    """.strip());
        }

        if (!normalizedColumns.contains("user_id")) {
            return Optional.of("users 테이블에 user_id 컬럼이 없습니다. 현재 엔티티 스키마와 DB 구조를 확인해주세요.");
        }

        if (!normalizedPrimaryKeys.contains("user_id")) {
            return Optional.of("users 테이블의 PK가 user_id가 아닙니다. 현재 엔티티 스키마와 DB 구조를 확인해주세요.");
        }

        return Optional.empty();
    }

    private boolean tableExists(DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(catalog, schema, null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String currentTableName = resultSet.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase(currentTableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<String> readColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {
        Set<String> columns = new LinkedHashSet<>();
        try (ResultSet resultSet = metaData.getColumns(catalog, schema, null, null)) {
            while (resultSet.next()) {
                String currentTableName = resultSet.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase(currentTableName)) {
                    columns.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        }
        return columns;
    }

    private Set<String> readPrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {
        Set<String> primaryKeys = new LinkedHashSet<>();
        try (ResultSet resultSet = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));
            }
        }

        if (!primaryKeys.isEmpty()) {
            return primaryKeys;
        }

        try (ResultSet resultSet = metaData.getPrimaryKeys(catalog, schema, tableName.toUpperCase(Locale.ROOT))) {
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    private static Set<String> normalize(Set<String> values) {
        return values.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
