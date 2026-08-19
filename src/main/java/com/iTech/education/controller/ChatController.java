package com.iTech.education.controller;

import com.iTech.education.dto.request.ChatMessageRequest;
import com.iTech.education.dto.request.StartChatRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.ConversationResponse;
import com.iTech.education.service.ChatService;
import com.iTech.education.websocket.ChatRealtimePublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@Validated
public class ChatController {

    private final ChatService chatService;
    private final ChatRealtimePublisher chatRealtimePublisher;

    public ChatController(ChatService chatService, ChatRealtimePublisher chatRealtimePublisher) {
        this.chatService = chatService;
        this.chatRealtimePublisher = chatRealtimePublisher;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@Valid @RequestBody StartChatRequest request,
                                   @RequestHeader(value = "X-Chat-Token", required = false) String headerToken,
                                   Authentication authentication) {
        if (request.getGuestToken() == null || request.getGuestToken().isBlank()) {
            request.setGuestToken(headerToken);
        }
        ConversationResponse conversation = chatService.start(request, email(authentication));
        chatRealtimePublisher.publish(conversation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã gửi tin nhắn", conversation));
    }

    @GetMapping("/conversation")
    public ResponseEntity<?> mine(@RequestHeader(value = "X-Chat-Token", required = false) String guestToken,
                                  Authentication authentication) {
        ConversationResponse conversation = chatService.getMine(guestToken, email(authentication));
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @PostMapping("/messages")
    public ResponseEntity<?> send(@Valid @RequestBody ChatMessageRequest request,
                                  @RequestHeader(value = "X-Chat-Token", required = false) String guestToken,
                                  Authentication authentication) {
        ConversationResponse conversation = chatService.sendVisitorMessage(request, guestToken, email(authentication));
        chatRealtimePublisher.publish(conversation);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi tin nhắn", conversation));
    }

    private String email(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())
                ? authentication.getName()
                : null;
    }
}
