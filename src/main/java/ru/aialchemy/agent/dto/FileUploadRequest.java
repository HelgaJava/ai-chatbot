package ru.aialchemy.agent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadRequest {
    private String fileName;
    private String originalFileName;
    private byte[] fileContent;
    private String mimeType;
    private String message;
    private String chatId;
    private String username;
    private Long fileSize;
}
