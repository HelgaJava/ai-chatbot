package ru.aialchemy.agent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRq {
    private String userQuestion;
    private String chatId;
    private String username;
}
