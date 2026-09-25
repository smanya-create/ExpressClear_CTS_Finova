package com.iispl.cts.common.config;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {

    private static Throwable initError;
    private static final String SUPABASE_HOST = "aws-0-ap-northeast-2.pooler.supabase.com";
    private static final String DB_NAME = "postgres";
    private static final int PORT = 6543;
    private static final String DB_USER = "postgres.wrqvispigpddkbanlxfw";
    private static final String DB_PASSWORD = "Imageinfo@123";

    private static volatile HikariDataSource dataSource;

    private static synchronized void initializeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            HikariConfig config = new HikariConfig();

            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:%d/%s?sslmode=require&prepareThreshold=0&preferQueryMode=simple&tcpKeepAlive=true",
                    SUPABASE_HOST,
                    PORT,
                    DB_NAME
            );

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(DB_USER.trim());
            config.setPassword(DB_PASSWORD.trim());
            config.setDriverClassName("org.postgresql.Driver");

            // Conservative pool sizing suited for Supabase pooler
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.setConnectionTimeout(20000);
            config.setValidationTimeout(4000);
            config.setLeakDetectionThreshold(10000);

            // Shorter idle timeout to release idle pooler connections promptly
            config.setIdleTimeout(120000); // 2 minutes
            config.setMaxLifetime(600000);  // 10 minutes

            // Critical: -1 prevents Hikari from permanently dying if initial ping fails
            config.setInitializationFailTimeout(-1);

            config.setPoolName("CTS-HikariPool");

            dataSource = new HikariDataSource(config);
            initError = null;
            System.out.println(">>> [HikariCP] DataSource successfully initialized.");

        } catch (Throwable e) {
            initError = e;
            System.err.println("CRITICAL: Failed to initialize HikariCP DataSource:");
            e.printStackTrace();
        }
    }

    static {
        initializeDataSource();
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initializeDataSource();
        }

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