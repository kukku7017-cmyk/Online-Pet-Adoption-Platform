package com.petadoption.dao;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.exception.DatabaseException;
import com.petadoption.model.Conversation;
import com.petadoption.model.Message;
import com.petadoption.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDAOImpl implements MessageDAO {

    @Override
    public Conversation findOrCreateConversation(Long applicationId, Long shelterId, Long adopterId) {
        Optional<Conversation> existing = findConversationByApplicationId(applicationId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String sql = "INSERT INTO conversations (application_id, shelter_id, adopter_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, applicationId);
            stmt.setLong(2, shelterId);
            stmt.setLong(3, adopterId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long convId = rs.getLong(1);
                    return findConversationById(convId).orElseThrow();
                }
            }
        } catch (SQLException e) {
            // In case of concurrent race condition, try to fetch again
            return findConversationByApplicationId(applicationId)
                .orElseThrow(() -> new DatabaseException("Error creating conversation", e));
        }
        throw new DatabaseException("Failed to retrieve conversation after insert");
    }

    @Override
    public Optional<Conversation> findConversationById(Long id) {
        String sql = """
            SELECT c.*,
                   u_sh.full_name AS shelter_name,
                   u_ad.full_name AS adopter_name,
                   p.name AS pet_name
            FROM conversations c
            JOIN users u_sh ON c.shelter_id = u_sh.id
            JOIN users u_ad ON c.adopter_id = u_ad.id
            JOIN adoption_applications a ON c.application_id = a.id
            JOIN pets p ON a.pet_id = p.id
            WHERE c.id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToConversation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding conversation by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Conversation> findConversationByApplicationId(Long applicationId) {
        String sql = """
            SELECT c.*,
                   u_sh.full_name AS shelter_name,
                   u_ad.full_name AS adopter_name,
                   p.name AS pet_name
            FROM conversations c
            JOIN users u_sh ON c.shelter_id = u_sh.id
            JOIN users u_ad ON c.adopter_id = u_ad.id
            JOIN adoption_applications a ON c.application_id = a.id
            JOIN pets p ON a.pet_id = p.id
            WHERE c.application_id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToConversation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding conversation by application ID: " + applicationId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Conversation> findConversationsByUserId(Long userId) {
        String sql = """
            SELECT c.*,
                   u_sh.full_name AS shelter_name,
                   u_ad.full_name AS adopter_name,
                   p.name AS pet_name
            FROM conversations c
            JOIN users u_sh ON c.shelter_id = u_sh.id
            JOIN users u_ad ON c.adopter_id = u_ad.id
            JOIN adoption_applications a ON c.application_id = a.id
            JOIN pets p ON a.pet_id = p.id
            WHERE c.shelter_id = ? OR c.adopter_id = ?
            ORDER BY c.last_message_at DESC
        """;
        List<Conversation> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToConversation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving conversations for user ID: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Message> findMessagesByConversationId(Long conversationId) {
        String sql = """
            SELECT m.*, u.full_name AS sender_name, u.role AS sender_role
            FROM messages m
            JOIN users u ON m.sender_id = u.id
            WHERE m.conversation_id = ?
            ORDER BY m.sent_at ASC
        """;
        List<Message> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding messages for conversation: " + conversationId, e);
        }
        return list;
    }

    @Override
    public Message createMessage(Message message) {
        String insertSql = "INSERT INTO messages (conversation_id, sender_id, recipient_id, content, is_read) VALUES (?, ?, ?, ?, ?)";
        String touchConvSql = "UPDATE conversations SET last_message_at = CURRENT_TIMESTAMP WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, message.getConversationId());
                stmt.setLong(2, message.getSenderId());
                stmt.setLong(3, message.getRecipientId());
                stmt.setString(4, message.getContent());
                stmt.setBoolean(5, message.getIsRead() != null && message.getIsRead());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        message.setId(rs.getLong(1));
                    }
                }
            }

            try (PreparedStatement stmtTouch = conn.prepareStatement(touchConvSql)) {
                stmtTouch.setLong(1, message.getConversationId());
                stmtTouch.executeUpdate();
            }

            conn.commit();
            return message;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error sending message", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public void markMessagesAsRead(Long conversationId, Long recipientId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE conversation_id = ? AND recipient_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            stmt.setLong(2, recipientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error marking messages read", e);
        }
    }

    @Override
    public long getUnreadCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE recipient_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching unread message count", e);
        }
        return 0;
    }

    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conv = new Conversation();
        conv.setId(rs.getLong("id"));
        conv.setApplicationId(rs.getLong("application_id"));
        conv.setShelterId(rs.getLong("shelter_id"));
        conv.setShelterName(rs.getString("shelter_name"));
        conv.setAdopterId(rs.getLong("adopter_id"));
        conv.setAdopterName(rs.getString("adopter_name"));
        conv.setPetName(rs.getString("pet_name"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) conv.setCreatedAt(created.toLocalDateTime());

        Timestamp lastMsg = rs.getTimestamp("last_message_at");
        if (lastMsg != null) conv.setLastMessageAt(lastMsg.toLocalDateTime());

        return conv;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getLong("id"));
        msg.setConversationId(rs.getLong("conversation_id"));
        msg.setSenderId(rs.getLong("sender_id"));
        msg.setRecipientId(rs.getLong("recipient_id"));
        msg.setContent(rs.getString("content"));
        msg.setIsRead(rs.getBoolean("is_read"));

        try {
            msg.setSenderName(rs.getString("sender_name"));
            msg.setSenderRole(User.Role.fromString(rs.getString("sender_role")));
        } catch (SQLException ignored) {}

        Timestamp sent = rs.getTimestamp("sent_at");
        if (sent != null) msg.setSentAt(sent.toLocalDateTime());

        return msg;
    }
}
