package com.iTech.education.controller;

import com.iTech.education.dto.request.ChatMessageRequest;
import com.iTech.education.dto.request.ChatWsMessageRequest;
import com.iTech.education.dto.response.ConversationResponse;
import com.iTech.education.service.ChatService;
import com.iTech.education.websocket.ChatPrincipal;
import com.iTech.education.websocket.ChatRealtimePublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;

@Controller
public class ChatWsController {

    private final ChatService chatService;
    private final ChatRealtimePublisher chatRealtimePublisher;

    public ChatWsController(ChatService chatService, ChatRealtimePublisher chatRealtimePublisher) {
        this.chatService = chatService;
        this.chatRealtimePublisher = chatRealtimePublisher;
    }

    @MessageMapping("/chat.send")
    public void send(ChatWsMessageRequest payload, Principal principal) {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setContent(payload.getContent());
        ConversationResponse conversation = chatService.sendVisitorMessage(
                request,
                guestToken(principal, payload.getGuestToken()),
                email(principal)
        );
        chatRealtimePublisher.publish(conversation);
    }

    @MessageMapping("/chat.staff.reply")
    public void staffReply(ChatWsMessageRequest payload, Principal principal) {
        ChatPrincipal chatPrincipal = principal instanceof ChatPrincipal value ? value : null;
        if (chatPrincipal == null || !chatPrincipal.isStaff() || payload.getConversationId() == null) {
            throw new IllegalArgumentException("Không có quyền trả lời chat");
        }
        ChatMessageRequest request = new ChatMessageRequest();
        request.setContent(payload.getContent());
        ConversationResponse conversation = chatService.replyAsStaff(
                payload.getConversationId(),
                request,
                chatPrincipal.email()
        );
        chatRealtimePublisher.publish(conversation);
    }

    @SendToUser("/queue/errors")
    @org.springframework.messaging.handler.annotation.MessageExceptionHandler
    public String onError(Exception ex) {
        return ex.getMessage();
    }

    private String email(Principal principal) {
        if (principal instanceof ChatPrincipal chatPrincipal) {
            return chatPrincipal.email();
        }
        return principal == null ? null : principal.getName();
    }

    private String guestToken(Principal principal, String payloadToken) {
        if (principal instanceof ChatPrincipal chatPrincipal && chatPrincipal.isGuest()) {
            return chatPrincipal.guestToken();
        }
        return StringUtils.hasText(payloadToken) ? payloadToken : null;
    }
}
