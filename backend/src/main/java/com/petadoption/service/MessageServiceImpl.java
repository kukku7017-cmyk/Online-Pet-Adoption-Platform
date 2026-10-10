package com.petadoption.service;

import com.petadoption.dao.MessageDAO;
import com.petadoption.dao.MessageDAOImpl;
import com.petadoption.exception.AuthorizationException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.Conversation;
import com.petadoption.model.Message;
import com.petadoption.model.User;
import com.petadoption.util.ValidationUtil;

import java.util.List;

public class MessageServiceImpl implements MessageService {

    private final MessageDAO messageDAO;

    public MessageServiceImpl() {
        this(new MessageDAOImpl());
    }

    public MessageServiceImpl(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    @Override
    public List<Conversation> getUserConversations(Long userId) {
        return messageDAO.findConversationsByUserId(userId);
    }

    @Override
    public List<Message> getConversationMessages(Long conversationId, User currentUser) {
        Conversation conv = messageDAO.findConversationById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        // Strict participant check (Prevents IDOR)
        if (!conv.getShelterId().equals(currentUser.getId()) &&
            !conv.getAdopterId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new AuthorizationException("You do not have permission to view this conversation.");
        }

        // Auto-mark unread incoming messages as read
        messageDAO.markMessagesAsRead(conversationId, currentUser.getId());

        return messageDAO.findMessagesByConversationId(conversationId);
    }

    @Override
    public Message sendMessage(Long conversationId, String content, User senderUser) {
        ValidationUtil.requireNonBlank(content, "Message content");

        Conversation conv = messageDAO.findConversationById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        if (!conv.getShelterId().equals(senderUser.getId()) &&
            !conv.getAdopterId().equals(senderUser.getId())) {
            throw new AuthorizationException("You are not an authorized participant in this conversation.");
        }

        Long recipientId = senderUser.getId().equals(conv.getShelterId()) ? conv.getAdopterId() : conv.getShelterId();

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderUser.getId());
        message.setRecipientId(recipientId);
        message.setContent(ValidationUtil.sanitize(content));
        message.setIsRead(false);

        return messageDAO.createMessage(message);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageDAO.getUnreadCount(userId);
    }
}
