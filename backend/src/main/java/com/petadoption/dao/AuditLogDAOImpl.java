package com.petadoption.dao;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.exception.DatabaseException;
import com.petadoption.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    @Override
    public void log(AuditLog auditLog) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (auditLog.getUserId() != null) {
                stmt.setLong(1, auditLog.getUserId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setString(2, auditLog.getAction());
            stmt.setString(3, auditLog.getEntityType());
            if (auditLog.getEntityId() != null) {
                stmt.setLong(4, auditLog.getEntityId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }
            stmt.setString(5, auditLog.getDetails());
            stmt.setString(6, auditLog.getIpAddress());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    auditLog.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating audit log entry", e);
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        String sql = """
            SELECT a.*, u.email AS user_email
            FROM audit_logs a
            LEFT JOIN users u ON a.user_id = u.id
            ORDER BY a.created_at DESC
            LIMIT ?
        """;
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getLong("id"));
                    long uId = rs.getLong("user_id");
                    if (!rs.wasNull()) log.setUserId(uId);
                    log.setUserEmail(rs.getString("user_email"));
                    log.setAction(rs.getString("action"));
                    log.setEntityType(rs.getString("entity_type"));
                    long eId = rs.getLong("entity_id");
                    if (!rs.wasNull()) log.setEntityId(eId);
                    log.setDetails(rs.getString("details"));
                    log.setIpAddress(rs.getString("ip_address"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) log.setCreatedAt(ts.toLocalDateTime());

                    list.add(log);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching recent audit logs", e);
        }
        return list;
    }
}
