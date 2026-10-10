package com.petadoption.dao;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.exception.DatabaseException;
import com.petadoption.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, password_hash, full_name, phone, address, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail().trim().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getStatus() != null ? user.getStatus().name() : User.UserStatus.ACTIVE.name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user: " + user.getEmail(), e);
        }
    }

    @Override
    public void updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, address = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, user.getAddress());
            stmt.setLong(4, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user profile ID: " + user.getId(), e);
        }
    }

    @Override
    public void updateStatus(Long userId, User.UserStatus status) {
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user status for ID: " + userId, e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(User.Role role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving users by role: " + role, e);
        }
        return list;
    }

    @Override
    public long countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting active admins", e);
        }
        return 0;
    }

    @Override
    public long countByRole(User.Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting users by role: " + role, e);
        }
        return 0;
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total users", e);
        }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setRole(User.Role.fromString(rs.getString("role")));
        user.setStatus(User.UserStatus.fromString(rs.getString("status")));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) user.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) user.setUpdatedAt(updated.toLocalDateTime());

        return user;
    }
}
