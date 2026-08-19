package com.iTech.education.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatStaffEvent {

    private String type;
    private long unread;
    private ConversationResponse conversation;
}
