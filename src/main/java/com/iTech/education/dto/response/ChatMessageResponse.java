package com.iTech.education.dto.response;

import com.iTech.education.entity.ChatMessage;
import com.iTech.education.utils.ChatSenderType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageResponse {

    private Long id;
    private ChatSenderType senderType;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponse fromEntity(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSenderType(message.getSenderType());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        if (message.getSenderType() == ChatSenderType.STAFF && message.getStaff() != null) {
            response.setSenderName(message.getStaff().getFullName());
        } else if (message.getSenderType() == ChatSenderType.SYSTEM) {
            response.setSenderName("LearnHub");
        } else if (message.getConversation() != null) {
            if (message.getConversation().getUser() != null) {
                response.setSenderName(message.getConversation().getUser().getFullName());
            } else {
                response.setSenderName(message.getConversation().getGuestName());
            }
        }
        return response;
    }
}
