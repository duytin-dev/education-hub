package com.iTech.education.dto.response;

import com.iTech.education.entity.Conversation;
import com.iTech.education.utils.ConversationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConversationResponse {

    private Long id;
    private String guestToken;
    private ConversationStatus status;
    private String visitorName;
    private String visitorEmail;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime lastMessageAt;
    private Integer unreadForStaff;
    private Integer unreadForVisitor;
    private String lastMessagePreview;
    private List<ChatMessageResponse> messages = new ArrayList<>();

    public static ConversationResponse fromEntity(Conversation conversation, boolean includeToken) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        if (includeToken) {
            response.setGuestToken(conversation.getGuestToken());
        }
        response.setStatus(conversation.getStatus());
        if (conversation.getUser() != null) {
            response.setVisitorName(conversation.getUser().getFullName());
            response.setVisitorEmail(conversation.getUser().getEmail());
        } else {
            response.setVisitorName(conversation.getGuestName());
            response.setVisitorEmail(conversation.getGuestEmail());
        }
        if (conversation.getCourse() != null) {
            response.setCourseId(conversation.getCourse().getId());
            response.setCourseTitle(conversation.getCourse().getTitle());
        }
        response.setLastMessageAt(conversation.getLastMessageAt());
        response.setUnreadForStaff(conversation.getUnreadForStaff());
        response.setUnreadForVisitor(conversation.getUnreadForVisitor());
        return response;
    }
}
