package com.iTech.education.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadAvatar(MultipartFile file) throws IOException;
    String uploadVideo(MultipartFile file) throws IOException;
}
