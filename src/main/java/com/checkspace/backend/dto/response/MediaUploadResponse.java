package com.checkspace.backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaUploadResponse {
    private String url;
    private String publicId;
}