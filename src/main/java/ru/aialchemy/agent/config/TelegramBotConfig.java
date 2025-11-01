package ru.aialchemy.agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.aialchemy.agent.bot.AiBot;
import ru.aialchemy.agent.service.FileUploadService;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsApi telegramBotsApi(AiBot aiBot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(aiBot);
            return botsApi;
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to register bot", e);
        }
    }

    @Bean
    public AiBot aiBot(FileUploadService fileUploadService) {
        return new AiBot(botToken, botUsername, fileUploadService);
    }
}
