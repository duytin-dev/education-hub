package com.iTech.education.dto.response;

import com.iTech.education.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponse {

    private Long id;
    private Long courseId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Integer rating;
    private Long parentId;
    private LocalDateTime createdAt;

    public static CommentResponse fromEntity(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setRating(comment.getRating());
        response.setCreatedAt(comment.getCreatedAt());

        if (comment.getCourse() != null) {
            response.setCourseId(comment.getCourse().getId());
        }
        if (comment.getUser() != null) {
            response.setUserId(comment.getUser().getId());
            response.setUserName(comment.getUser().getFullName());
            response.setUserAvatar(comment.getUser().getAvatar());
        }
        if (comment.getParent() != null) {
            response.setParentId(comment.getParent().getId());
        }

        return response;
    }
}
