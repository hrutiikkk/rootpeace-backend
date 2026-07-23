package com.checkspace.backend.dto.request;

import lombok.Data;

@Data
public class MediaItemRequest {
    private String url;
    private String publicId;
    private String label; // only used for documents, e.g. "Sale deed"
}