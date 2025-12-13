package com.example.ecommerse_college;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.github.cdimascio.dotenv.Dotenv;




/**
 * Simple JDBC helper for the remote MySQL database (credentials taken from the provided screenshot).
 *
 * Note:
 * - You must have the MySQL Connector/J driver on the classpath (Maven: add dependency for
 *   `mysql:mysql-connector-java`).
 * - For production, never hardcode credentials; use environment variables or a secure vault.
 */
public class Database {

	// Load DB configuration from environment or .env file
	private static final String HOST;
	private static final int PORT;
	private static final String DATABASE;
	private static final String USER;
	private static final String PASSWORD;
	private static final String JDBC_URL;

	static {
		Dotenv dotenv = null;
		try {
			dotenv = Dotenv.configure().ignoreIfMissing().load();
		} catch (Throwable t) {
			// ignore — we'll fall back to System.getenv
		}

		HOST = getenv(dotenv, "DB_HOST");
		String portStr = getenv(dotenv, "DB_PORT");
		DATABASE = getenv(dotenv, "DB_NAME");
		USER = getenv(dotenv, "DB_USER");
		PASSWORD = getenv(dotenv, "DB_PASSWORD");

		if (HOST == null || portStr == null || DATABASE == null || USER == null || PASSWORD == null) {
			throw new IllegalStateException("Database credentials not found. Create a .env file or set environment variables: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD");
		}
		int portTmp;
		try {
			portTmp = Integer.parseInt(portStr);
		} catch (NumberFormatException nfe) {
			throw new IllegalStateException("DB_PORT must be a valid integer", nfe);
		}
		PORT = portTmp;

		JDBC_URL = String.format(
				"jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
				HOST, PORT, DATABASE);
	}

	/**
	 * Get a new JDBC connection to the configured database.
	 * Caller is responsible for closing the returned Connection.
	 */
	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("MySQL JDBC Driver not found on classpath", e);
		}
		return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
	}

	/**
	 * Close JDBC resources quietly.
	 */
	public static void closeQuietly(AutoCloseable ac) {
		if (ac == null) return;
		try {
			ac.close();
		} catch (Exception ignored) {
		}
	}

	/**
	 * Insert a new user into the `user` table.
	 * Returns true if the row was inserted.
	 *
	 * Note: Passwords are stored in plain text here only for simplicity to match your
	 * existing schema. For real apps, hash passwords (e.g. bcrypt) before storing.
	 */
	public static boolean addUser(String userName, String password, boolean isAdmin) throws SQLException {
		// Compute next id: if table empty -> 1, else max(id)+1
		final String nextIdSql = "SELECT COALESCE(MAX(id),0)+1 AS next_id FROM user";
		try (Connection conn = getConnection();
			 java.sql.PreparedStatement idPs = conn.prepareStatement(nextIdSql);
			 java.sql.ResultSet rs = idPs.executeQuery()) {
			int nextId = 1;
			if (rs.next()) {
				nextId = rs.getInt("next_id");
			}

			final String insertSql = "INSERT INTO user (id, user_name, password, isAdmin) VALUES (?, ?, ?, ?)";
			try (java.sql.PreparedStatement ps = conn.prepareStatement(insertSql)) {
				ps.setInt(1, nextId);
				ps.setString(2, userName);
				ps.setString(3, password);
				ps.setInt(4, isAdmin ? 1 : 0);
				int affected = ps.executeUpdate();
				return affected == 1;
			}
		}
	}

	/**
	 * Retrieve a user by username and password. Returns a User object or null when not found.
	 */
	public static User getUserByCredentials(String userName, String password) throws SQLException {
		final String sql = "SELECT id, user_name, password, isAdmin FROM user WHERE user_name = ? AND password = ? LIMIT 1";
		try (Connection conn = getConnection();
			 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userName);
			ps.setString(2, password);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new User(
							rs.getInt("id"),
							rs.getString("user_name"),
							rs.getString("password"),
							rs.getInt("isAdmin") != 0
					);
				}
			}
		}
		return null;
	}

	/**
	 * Simple immutable holder for a user row.
	 */
	public static class User {
		public final int id;
		public final String userName;
		public final String password;
		public final boolean isAdmin;

		public User(int id, String userName, String password, boolean isAdmin) {
			this.id = id;
			this.userName = userName;
			this.password = password;
			this.isAdmin = isAdmin;
		}

		@Override
		public String toString() {
			return "User{id=" + id + ", userName='" + userName + "', isAdmin=" + isAdmin + "}";
		}
	}


	// Helper: read key from system env first, then dotenv (if available)
	private static String getenv(Dotenv dotenv, String key) {
		String v = System.getenv(key);
		if (v != null && !v.isEmpty()) return v;
		if (dotenv != null) {
			v = dotenv.get(key);
			if (v != null && !v.isEmpty()) return v;
		}
		return null;
	}
}

