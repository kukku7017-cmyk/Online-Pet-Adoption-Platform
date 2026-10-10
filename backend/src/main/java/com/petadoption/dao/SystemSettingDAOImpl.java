package com.petadoption.dao;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.exception.DatabaseException;
import com.petadoption.model.SystemSetting;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SystemSettingDAOImpl implements SystemSettingDAO {

    @Override
    public Optional<SystemSetting> findByKey(String key) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSetting(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding system setting: " + key, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SystemSetting> findAll() {
        String sql = "SELECT * FROM system_settings ORDER BY setting_key ASC";
        List<SystemSetting> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSetting(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving system settings", e);
        }
        return list;
    }

    @Override
    public void updateValue(String key, String value) {
        String sql = "UPDATE system_settings SET setting_value = ?, updated_at = CURRENT_TIMESTAMP WHERE setting_key = ? AND is_editable = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating system setting: " + key, e);
        }
    }

    private SystemSetting mapResultSetToSetting(ResultSet rs) throws SQLException {
        SystemSetting s = new SystemSetting();
        s.setSettingKey(rs.getString("setting_key"));
        s.setSettingValue(rs.getString("setting_value"));
        s.setDescription(rs.getString("description"));
        s.setIsEditable(rs.getBoolean("is_editable"));

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());

        return s;
    }
}
