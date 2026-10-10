package com.petadoption.dao;

import com.petadoption.model.Conversation;
import com.petadoption.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageDAO {
    Conversation findOrCreateConversation(Long applicationId, Long shelterId, Long adopterId);
    Optional<Conversation> findConversationById(Long id);
    Optional<Conversation> findConversationByApplicationId(Long applicationId);
    List<Conversation> findConversationsByUserId(Long userId);
    List<Message> findMessagesByConversationId(Long conversationId);
    Message createMessage(Message message);
    void markMessagesAsRead(Long conversationId, Long recipientId);
    long getUnreadCount(Long userId);
}
