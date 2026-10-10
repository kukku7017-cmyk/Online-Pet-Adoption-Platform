package com.petadoption.service;

import com.petadoption.model.Conversation;
import com.petadoption.model.Message;
import com.petadoption.model.User;

import java.util.List;

public interface MessageService {
    List<Conversation> getUserConversations(Long userId);
    List<Message> getConversationMessages(Long conversationId, User currentUser);
    Message sendMessage(Long conversationId, String content, User senderUser);
    long getUnreadCount(Long userId);
}
