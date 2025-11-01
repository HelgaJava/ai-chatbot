package ru.aialchemy.agent.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.aialchemy.agent.bot.command.base.BotCommand;
import ru.aialchemy.agent.bot.command.impl.AboutCommand;
import ru.aialchemy.agent.bot.command.impl.HelpCommand;
import ru.aialchemy.agent.bot.command.impl.StartCommand;
import ru.aialchemy.agent.service.FileUploadService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AiBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<String, BotCommand> callbackCommands = new HashMap<>();
    private final FileUploadService fileUploadService;


    public AiBot(String botToken, String botUsername, FileUploadService fileUploadService) {
        super(botToken);
        this.botUsername = botUsername;
        this.fileUploadService = fileUploadService;
        initializeCommands();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void initializeCommands() {
        // Текстовые команды
        registerCommand(new StartCommand());
        registerCommand(new HelpCommand());
        registerCommand(new AboutCommand());

        // Callback команды
        callbackCommands.put("help", new HelpCommand());
        callbackCommands.put("about", new AboutCommand());
    }

    private void registerCommand(BotCommand command) {
        commands.put(command.getCommandIdentifier(), command);
    }


    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            } else if (update.hasMessage()) {
                var message = update.getMessage();
                if (message.hasText()) {
                    handleTextMessage(update);
                } else if (message.hasDocument() || message.hasPhoto()) {
                    handleFileMessage(update);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Error processing update: {}", e.getMessage());
        }

    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        var callback = update.getCallbackQuery();
        var data = callback.getData();

        log.info("Received callback: '{}' from user: {}", data, callback.getFrom().getUserName());

        var command = callbackCommands.get(data);
        if (command != null) {
            command.executeCommand(this, update);
        } else {
            log.warn("Unknown callback data: {}", data);
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        var messageText = update.getMessage().getText().trim();
        var chatId = update.getMessage().getChatId();
        var userName = update.getMessage().getChat().getUserName();

        log.info("Received message: '{}' from user: {} chat: {}", messageText, userName, chatId);

        // Проверяем команды
        if (messageText.startsWith("/")) {
            var commandName = messageText.substring(1).toLowerCase();
            var command = commands.get(commandName);

            if (command != null) {
                command.executeCommand(this, update);
            } else {
                sendEchoMessage(chatId, messageText);
            }
        } else {
            sendEchoMessage(chatId, messageText);
        }
    }

    private boolean sendEchoMessage(Long chatId, String messageText) {
        var responseText = """
                🔄 Эхо:
                %s
                            
                Используй кнопки ниже для других действий.
                """.formatted(messageText);

        var keyboard = createInlineKeyboard();
        return sendMessage(chatId, responseText, "echo response", keyboard);
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        var helpButton = InlineKeyboardButton.builder()
                .text("📋 Help")
                .callbackData("help")
                .build();

        var aboutButton = InlineKeyboardButton.builder()
                .text("ℹ️ About")
                .callbackData("about")
                .build();

        var row = List.of(helpButton, aboutButton);

        var keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();

        return keyboard;
    }

    private boolean sendMessage(Long chatId, String text, String messageType, InlineKeyboardMarkup keyboard) {
        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(keyboard)
                .build();

        try {
            execute(message);
            log.info("Sent {} to chat: {}", messageType, chatId);
            return true;
        } catch (TelegramApiException e) {
            log.error("Error sending {}: {}", messageType, e.getMessage());
            return false;
        }
    }

    private void handleFileMessage(Update update) throws TelegramApiException {
        var message = update.getMessage();
        var chatId = message.getChatId();
        var userName = message.getChat().getUserName();
        var caption = message.getCaption(); // Текст, сопровождающий файл

        log.info("Received file from user: {} chat: {}", userName, chatId);

        try {
            // Получаем Telegram File объект
            File telegramFile = getFileFromMessage(message);
            if (telegramFile == null) {
                sendErrorMessage(chatId, "Не удалось обработать файл");
                return;
            }

            // Скачиваем файл
            byte[] fileContent = downloadTelegramFile(telegramFile);
            if (fileContent == null) {
                sendErrorMessage(chatId, "Не удалось скачать файл");
                return;
            }

            // Отправляем уведомление о начале обработки
            sendProcessingMessage(chatId, getOriginalFileName(message));

            // Отправляем файл через HTTP как multipart/form-data
            var serviceResponse = fileUploadService.uploadFile(
                    getOriginalFileName(message),
                    fileContent,
                    getMimeType(message),
                    caption != null ? caption : "Файл без описания",
                    chatId.toString(),
                    userName
            );
            // Отправляем ответ от сервиса пользователю
            sendServiceResponse(chatId, getOriginalFileName(message), serviceResponse);

        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage());
            sendErrorMessage(chatId, "❌ Ошибка при обработке файла: " + e.getMessage());
        }
    }

    private File getFileFromMessage(Message message) throws TelegramApiException {
        if (message.hasDocument()) {
            // Для документов получаем полную информацию о файле
            String fileId = message.getDocument().getFileId();
            return execute(GetFile.builder().fileId(fileId).build());
        } else if (message.hasPhoto()) {
            // Для фото берем самую качественную версию и получаем полную информацию
            var photos = message.getPhoto();
            String fileId = photos.get(photos.size() - 1).getFileId();
            return execute(GetFile.builder().fileId(fileId).build());
        }
        return null;
    }


    private byte[] downloadTelegramFile(File telegramFile) {
        try {
            String filePath = telegramFile.getFilePath();
            if (filePath == null) {
                log.warn("File path is null for file: {}", telegramFile.getFileId());
                return null;
            }

            // Скачиваем файл используя полный URL
            try (InputStream inputStream = downloadFileAsStream(telegramFile)) {
                return inputStream.readAllBytes();
            }

        } catch (Exception e) {
            log.error("Error downloading Telegram file: {}", e.getMessage());
            return null;
        }
    }

    private String getOriginalFileName(Message message) {
        if (message.hasDocument()) {
            return message.getDocument().getFileName();
        } else if (message.hasPhoto()) {
            return "photo.jpg";
        }
        return "file";
    }

    private String getMimeType(Message message) {
        if (message.hasDocument()) {
            return message.getDocument().getMimeType();
        } else if (message.hasPhoto()) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }


    private void sendProcessingMessage(Long chatId, String fileName) throws TelegramApiException {
        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("🔄 Обрабатываю файл: " + fileName + "\n\n⏳ Пожалуйста, подождите...")
                .build();

        execute(message);
        log.info("Sent processing message to chat: {}", chatId);
    }

    private void sendServiceResponse(Long chatId, String fileName, String serviceResponse) throws TelegramApiException {
        var responseText = """
        ✅ Обработка завершена!
        
        📎 Файл: %s
        
        📋 Ответ сервиса:
        %s
        """.formatted(fileName, serviceResponse);

        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(responseText)
                .replyMarkup(createInlineKeyboard())
                .build();

        execute(message);
        log.info("Sent service response to chat: {}", chatId);
    }

    private void sendErrorMessage(Long chatId, String text) throws TelegramApiException {
        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(createInlineKeyboard())
                .build();

        execute(message);
        log.error("Sent file error message to chat: {}", chatId);
    }
}
