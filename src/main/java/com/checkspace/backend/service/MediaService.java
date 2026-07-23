package com.checkspace.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.checkspace.backend.dto.response.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final Cloudinary cloudinary;

    public MediaUploadResponse upload(MultipartFile file, String resourceType) throws Exception {
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", "checkspace/" + resourceType.toLowerCase(),
                "resource_type", resourceType.equalsIgnoreCase("video") ? "video" : "auto"
        );

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

        return MediaUploadResponse.builder()
                .url((String) result.get("secure_url"))
                .publicId((String) result.get("public_id"))
                .build();
    }
}