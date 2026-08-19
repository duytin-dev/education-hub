package com.iTech.education.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatWsMessageRequest {

    private String content;
    private String guestToken;
    private Long conversationId;
}
