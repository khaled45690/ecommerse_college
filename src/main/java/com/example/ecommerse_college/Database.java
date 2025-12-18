package com.example.ecommerse_college;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("MySQL JDBC Driver not found on classpath", e);
		}
		return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
	}

	public static void closeQuietly(AutoCloseable ac) {
		if (ac == null) return;
		try {
			ac.close();
		} catch (Exception ignored) {
		}
	}


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


	/**
	 * Product POJO for the `products` table.
	 */
	public static class Product {
		public final int id;
		public final String name;
		public final BigDecimal price;

		public Product(int id, String name, BigDecimal price) {
			this.id = id;
			this.name = name;
			this.price = price;
		}

		@Override
		public String toString() {
			return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
		}
	}

	/**
	 * Add a product to the `products` table. Returns true if the insert succeeded.
	 */
	public static boolean addProduct(String name, BigDecimal price) throws SQLException {
		final String nextIdSql = "SELECT COALESCE(MAX(id),0)+1 AS next_id FROM products";
		try (Connection conn = getConnection();
			 java.sql.PreparedStatement idPs = conn.prepareStatement(nextIdSql);
			 java.sql.ResultSet rs = idPs.executeQuery()) {
			int nextId = 1;
			if (rs.next()) nextId = rs.getInt("next_id");

			final String insertSql = "INSERT INTO products (id, name, price) VALUES (?, ?, ?)";
			try (java.sql.PreparedStatement ps = conn.prepareStatement(insertSql)) {
				ps.setInt(1, nextId);
				ps.setString(2, name);
				ps.setBigDecimal(3, price);
				int affected = ps.executeUpdate();
				return affected == 1;
			}
		}
	}

	/**
	 * Delete a product by id. Returns true if a row was deleted.
	 */
	public static boolean deleteProduct(int id) throws SQLException {
		final String sql = "DELETE FROM products WHERE id = ?";
		try (Connection conn = getConnection();
			 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			int affected = ps.executeUpdate();
			return affected == 1;
		}
	}

	/**
	 * Update a product's name and price by id. Returns true if updated.
	 */
	public static boolean updateProduct(int id, String name, BigDecimal price) throws SQLException {
		final String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
		try (Connection conn = getConnection();
			 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, name);
			ps.setBigDecimal(2, price);
			ps.setInt(3, id);
			int affected = ps.executeUpdate();
			return affected == 1;
		}
	}

	/**
	 * Retrieve all products from the `products` table.
	 */
	public static List<Product> getAllProducts() throws SQLException {
		final String sql = "SELECT id, name, price FROM products ORDER BY id";
		List<Product> out = new ArrayList<>();
		try (Connection conn = getConnection();
			 java.sql.PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				out.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price")));
			}
		}
		return out;
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
// --- Checkouts support ---

    /**
     * Checkout POJO for the `checkouts` table.
     */
    public static class Checkout {
        public final int id;
        public final String productsJson;
        public final LocalDateTime date;

        public Checkout(int id, String productsJson, LocalDateTime date) {
            this.id = id;
            this.productsJson = productsJson;
            this.date = date;
        }

        @Override
        public String toString() {
            return "Checkout{id=" + id + ", date=" + date + ", products=" + productsJson + "}";
        }
    }

    /**
     * Save a checkout row with the provided products list (serialized as JSON) and
     * use the database current timestamp for the `date` column. Returns the saved
     * Checkout with the DB timestamp, or null on failure.
     */
    public static Checkout saveCheckout(List<Product> products) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (Product p : products) {
            if (!first) sb.append(',');
            first = false;
            // Simple escaping for quotes in name
            String escapedName = p.name.replace("\\", "\\\\").replace("\"", "\\\"");
            sb.append('{')
              .append("\"id\":").append(p.id).append(',')
              .append("\"name\":\"").append(escapedName).append("\",")
              .append("\"price\":\"").append(p.price.toPlainString()).append("\"")
              .append('}');
        }
        sb.append(']');
        String productsJson = sb.toString();

        final String nextIdSql = "SELECT COALESCE(MAX(id),0)+1 AS next_id FROM checkouts";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement idPs = conn.prepareStatement(nextIdSql);
             ResultSet rs = idPs.executeQuery()) {
            int nextId = 1;
            if (rs.next()) nextId = rs.getInt("next_id");

            final String insertSql = "INSERT INTO checkouts (id, products, date) VALUES (?, ?, NOW())";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, nextId);
                ps.setString(2, productsJson);
                int affected = ps.executeUpdate();
                if (affected != 1) return null;
            }

            // Retrieve the DB timestamp that was stored
            final String sel = "SELECT date FROM checkouts WHERE id = ? LIMIT 1";
            try (java.sql.PreparedStatement ps2 = conn.prepareStatement(sel)) {
                ps2.setInt(1, nextId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        Timestamp t = rs2.getTimestamp("date");
                        LocalDateTime dt = t != null ? t.toLocalDateTime() : null;
                        return new Checkout(nextId, productsJson, dt);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieve all checkouts.
     */
    public static List<Checkout> getAllCheckouts() throws SQLException {
        final String sql = "SELECT id, products, date FROM checkouts ORDER BY id";
        List<Checkout> out = new ArrayList<>();
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String productsJson = rs.getString("products");
                Timestamp t = rs.getTimestamp("date");
                LocalDateTime dt = t != null ? t.toLocalDateTime() : null;
                out.add(new Checkout(id, productsJson, dt));
            }
        }
        return out;
    }

}

