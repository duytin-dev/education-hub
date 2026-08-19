package com.iTech.education.websocket;

import com.iTech.education.dto.response.ChatStaffEvent;
import com.iTech.education.dto.response.ConversationResponse;
import com.iTech.education.service.ChatService;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatRealtimePublisher(SimpMessagingTemplate messagingTemplate, @Lazy ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    public void publish(ConversationResponse conversation) {
        if (conversation == null || conversation.getId() == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/chat." + conversation.getId(), conversation);

        ConversationResponse staffView = conversation;
        staffView.setGuestToken(null);
        messagingTemplate.convertAndSend("/topic/chat.staff", new ChatStaffEvent(
                "UPDATED",
                chatService.unreadForStaff(),
                staffView
        ));
    }
}
