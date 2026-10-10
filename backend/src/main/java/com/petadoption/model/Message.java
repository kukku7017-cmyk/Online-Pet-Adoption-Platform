package com.petadoption.model;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private User.Role senderRole;
    private Long recipientId;
    private String content;
    private Boolean isRead = false;
    private LocalDateTime sentAt;

    public Message() {}

    public Message(Long conversationId, Long senderId, Long recipientId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.isRead = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public User.Role getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(User.Role senderRole) {
        this.senderRole = senderRole;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead != null ? isRead : false;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
