package com.iispl.cts.common.config;

import java.sql.Connection;


import java.sql.SQLException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

public class DBConnection {

    private static final String SUPABASE_HOST = "aws-0-ap-northeast-2.pooler.supabase.com"; 
    private static final String DB_NAME = "postgres";
    private static final int PORT = 6543; 
    
    // Explicit project-tenant username
    private static final String DB_USER = "postgres.wrqvispigpddkbanlxfw"; 
    private static final String DB_PASSWORD = "Imageinfo@123"; 

    private static DataSource dataSource;

    static {
        try {
            PGSimpleDataSource pgds = new PGSimpleDataSource();
            
            pgds.setServerNames(new String[]{SUPABASE_HOST});
            pgds.setDatabaseName(DB_NAME);
            pgds.setPortNumbers(new int[]{PORT});
            
            // Apply .trim() to ensure no newline or whitespace characters are sent
            pgds.setUser(DB_USER.trim());
            pgds.setPassword(DB_PASSWORD.trim());
            
            pgds.setSslMode("require");
            pgds.setConnectTimeout(10);
            pgds.setSocketTimeout(30);
            pgds.setPrepareThreshold(0);
            dataSource = pgds;

            System.out.println("PostgreSQL DataSource initialized successfully.");

        } catch (Exception e) {
            System.err.println("Failed to initialize PostgreSQL DataSource.");
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized properly.");
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
}