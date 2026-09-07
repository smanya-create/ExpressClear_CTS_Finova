package com.iispl.cts.common.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {

    private static Throwable initError;

    private static final String SUPABASE_HOST = System.getenv().getOrDefault(
            "DB_HOST", "aws-0-ap-northeast-2.pooler.supabase.com");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "postgres");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "6543"));
    private static final String DB_USER = System.getenv().getOrDefault(
            "DB_USER", "postgres.wrqvispigpddkbanlxfw");
    private static final String DB_PASSWORD = System.getenv().getOrDefault(
            "DB_PASSWORD", "");

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:%d/%s?sslmode=require&prepareThreshold=0&preferQueryMode=simple",
                    SUPABASE_HOST, PORT, DB_NAME
            );

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(DB_USER.trim());
            config.setPassword(DB_PASSWORD.trim());
            config.setDriverClassName("org.postgresql.Driver");

            config.setConnectionTimeout(30000);
            config.setValidationTimeout(5000);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(120000);
            config.setInitializationFailTimeout(-1);
            config.setPoolName("CTS-HikariPool");

            dataSource = new HikariDataSource(config);

        } catch (Throwable e) {
            initError = e;
            System.err.println("CRITICAL: Failed to initialize HikariCP DataSource:");
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            String cause = (initError != null) ? initError.getMessage() : "Unknown init failure";
            throw new SQLException("DataSource is not initialized properly. Cause: " + cause, initError);
        }
        return dataSource.getConnection();
    }

    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                System.out.println("HikariCP Connection Pool closed successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}