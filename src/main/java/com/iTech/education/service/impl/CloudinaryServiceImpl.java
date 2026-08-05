package com.iTech.education.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.iTech.education.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadAvatar(MultipartFile file)
            throws IOException {
        Map result = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("folder", "avatars"));
        return result
                .get("secure_url")
                .toString();

    }



}
