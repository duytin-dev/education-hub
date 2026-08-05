package com.iTech.education.service.impl;

import com.iTech.education.repository.CommentRepository;
import com.iTech.education.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    public CommentServiceImpl(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }
}
