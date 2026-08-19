package com.iTech.education.controller;

import com.iTech.education.dto.request.ChatMessageRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.service.ChatService;
import com.iTech.education.websocket.ChatRealtimePublisher;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/chat")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminChatController {

    private final ChatService chatService;
    private final ChatRealtimePublisher chatRealtimePublisher;

    public AdminChatController(ChatService chatService, ChatRealtimePublisher chatRealtimePublisher) {
        this.chatService = chatService;
        this.chatRealtimePublisher = chatRealtimePublisher;
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(ApiResponse.success(chatService.listForStaff()));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> unread() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", chatService.unreadForStaff())));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getForStaff(id)));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<?> reply(@PathVariable Long id,
                                   @Valid @RequestBody ChatMessageRequest request,
                                   Authentication authentication) {
        var conversation = chatService.replyAsStaff(id, request, authentication.getName());
        chatRealtimePublisher.publish(conversation);
        return ResponseEntity.ok(ApiResponse.success("Đã trả lời", conversation));
    }

    @PatchMapping("/conversations/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id) {
        var conversation = chatService.close(id);
        chatRealtimePublisher.publish(conversation);
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đóng cuộc trò chuyện",
                conversation
        ));
    }
}
