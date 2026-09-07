package com.iispl.cts.common.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {

	private static final String SUPABASE_HOST = "aws-0-ap-northeast-2.pooler.supabase.com";

	private static final String DB_NAME = "postgres";

	private static final int PORT = 5432;

	private static final String DB_USER = "postgres.wrqvispigpddkbanlxfw";

	private static final String DB_PASSWORD = "Imageinfo@123";

	private static HikariDataSource dataSource;

	static {
		try {

			HikariConfig config = new HikariConfig();

			String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", SUPABASE_HOST, PORT, DB_NAME);

			config.setJdbcUrl(jdbcUrl);
			config.setUsername(DB_USER.trim());
			config.setPassword(DB_PASSWORD.trim());
			config.setDriverClassName("org.postgresql.Driver");

			config.setMaximumPoolSize(5);
			config.setMinimumIdle(1);

			config.setConnectionTimeout(30000);
			config.setValidationTimeout(5000);

			config.setIdleTimeout(60000);
			config.setMaxLifetime(300000);

			config.setPoolName("CTS-HikariPool");

			dataSource = new HikariDataSource(config);

			System.out.println("======================================");
			System.out.println(" HikariCP Connection Pool ACTIVE");
			System.out.println(" Supabase Host : " + SUPABASE_HOST);
			System.out.println(" Port          : " + PORT);
			System.out.println(" Max Pool Size : 5");
			System.out.println("======================================");

		} catch (Exception e) {

			System.err.println("Failed to initialize HikariCP DataSource.");

			e.printStackTrace();

			dataSource = null;
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