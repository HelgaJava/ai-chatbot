package ru.aialchemy.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.aialchemy.agent.dto.FileUploadRequest;

@Service
@Slf4j
public class FileUploadService {
    private final RestTemplate restTemplate;
    private final String fileServiceUrl;

    public FileUploadService(RestTemplate restTemplate,
                             @Value("${file.upload.service.url}")String fileServiceUrl) {
        this.restTemplate = restTemplate;
        this.fileServiceUrl = fileServiceUrl;
    }


    public boolean uploadFile(FileUploadRequest request) {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));

            var entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    fileServiceUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("File successfully uploaded to external service: {}", request.getFileName());
                return true;
            } else {
                log.error("Failed to upload file. Status: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Error uploading file to external service: {}", e.getMessage());
            return false;
        }
    }

}
