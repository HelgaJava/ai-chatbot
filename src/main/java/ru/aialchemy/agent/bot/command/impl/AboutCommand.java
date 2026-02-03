package ru.aialchemy.agent.bot.command.impl;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.aialchemy.agent.bot.command.base.AbstractBotCommand;

import java.util.List;

@Slf4j
public class AboutCommand extends AbstractBotCommand {
    @Override
    public void executeCommand(AbsSender absSender, Update update) throws TelegramApiException {
        var chatId = getChatId(update);

        var aboutText = """
            ℹ️ Обо мне:
            
            Я AI-агент, созданный для проведения аналитики и редактирования отчетов в формате Word.
            Технологии создания:
            • Spring Boot
            • Telegram Bot API
            • Java 17
            
            Для получения инструкций по использованию воспользуйтесь кнопкой Help.
            """;

        var keyboard = createInlineKeyboard();
        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(aboutText)
                .replyMarkup(keyboard)
                .build();

        absSender.execute(message);
        log.info("Sent about message to chat: {}", chatId);

    }

    @Override
    public String getCommandIdentifier() {
        return "about";
    }

    private InlineKeyboardMarkup createInlineKeyboard() {

        var helpButton = InlineKeyboardButton.builder()
                .text("📋 Help")
                .callbackData("help")
                .build();

        var row = List.of(helpButton);

        return InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();
    }
}
