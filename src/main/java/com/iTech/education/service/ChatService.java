package com.iTech.education.service;

import com.iTech.education.dto.request.ChatMessageRequest;
import com.iTech.education.dto.request.StartChatRequest;
import com.iTech.education.dto.response.ConversationResponse;

import java.util.List;

public interface ChatService {

    ConversationResponse start(StartChatRequest request, String currentUserEmail);

    ConversationResponse getMine(String guestToken, String currentUserEmail);

    ConversationResponse sendVisitorMessage(ChatMessageRequest request, String guestToken, String currentUserEmail);

    List<ConversationResponse> listForStaff();

    ConversationResponse getForStaff(Long id);

    ConversationResponse replyAsStaff(Long id, ChatMessageRequest request, String staffEmail);

    ConversationResponse close(Long id);

    long unreadForStaff();
}
