package com.iTech.education.dto.response;

import com.iTech.education.entity.User;
import com.iTech.education.utils.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String avatar;
    private String phone;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatar(),
                user.getPhone(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}
