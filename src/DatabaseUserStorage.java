import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// DatabaseUserStorage.java: connects the login system to your MySQL database.
//
// Uses your existing DatabaseConnection (accutrack_db, root/Brent200623.)
// Stores password hashes (SHA-256), never plain text.
// Table schema: users(user_id INT PK AI, username VARCHAR(50) UNIQUE, password_hash VARCHAR(255))
public class DatabaseUserStorage implements UserStorage {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // hash the password with SHA-256 (hex string, 64 chars)
    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("usernameExists error: " + e.getMessage());
            return false;
        }
    }

    @Override public boolean addUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, username);
            ps.setString(2, hash(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("addUser error: " + e.getMessage());
            return false;
        }
    }

    @Override public String checkLogin(String username, String password) {
        String sql = "SELECT username FROM users WHERE LOWER(username) = LOWER(?) AND password_hash = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return null;
            ps.setString(1, username);
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("checkLogin error: " + e.getMessage());
            return null;
        }
    }

    @Override public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, hash(newPassword));
            ps.setString(2, username);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("updatePassword error: " + e.getMessage());
            return false;
        }
    }
}
