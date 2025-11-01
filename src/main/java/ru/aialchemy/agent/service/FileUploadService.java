package ru.aialchemy.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.aialchemy.agent.dto.UserRq;

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


    public String uploadFile(String originalFileName, byte[] fileContent, String mimeType,
                              String message, String chatId, String username) {
        try {
            // Создаем UserRq объект
            var userRq = UserRq.builder()
                    .userQuestion(message)
                    .chatId(chatId)
                    .username(username)
                    .build();

            // Создаем multipart запрос
            var body = createMultipartBody(originalFileName, fileContent, mimeType, userRq);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            var entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    fileServiceUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("File successfully uploaded to document service: {}", originalFileName);
                log.info("Service response: {}", response.getBody());
                return  response.getBody() != null ? response.getBody() : "Файл отправлен, но ответ от сервиса не получен";
            } else {
                log.error("Failed to upload file. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                return "Ошибка сервиса: " + response.getStatusCode();
            }

        } catch (Exception e) {
            log.error("Error uploading file to document service: {}", e.getMessage());
            return "Ошибка соединения: " + e.getMessage();
        }
    }

    private MultiValueMap<String, Object> createMultipartBody(String fileName, byte[] fileContent,
                                                              String mimeType, UserRq userRq) {
        var body = new LinkedMultiValueMap<String, Object>();

        // Добавляем файл
        var fileResource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        body.add("file", fileResource);

        // Добавляем JSON объект userRq
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var userRqEntity = new HttpEntity<>(userRq, headers);
        body.add("userRq", userRqEntity);

        return body;
    }

}
