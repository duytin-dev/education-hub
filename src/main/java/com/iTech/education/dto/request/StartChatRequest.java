package com.iTech.education.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartChatRequest {

    @Size(max = 120)
    private String guestName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255)
    private String guestEmail;

    private String guestToken;

    private Long courseId;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000, message = "Nội dung không được vượt quá 2000 ký tự")
    private String content;
}
