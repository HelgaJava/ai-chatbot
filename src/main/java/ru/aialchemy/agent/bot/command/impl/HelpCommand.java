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
public class HelpCommand extends AbstractBotCommand {
    @Override
    public void executeCommand(AbsSender absSender, Update update) throws TelegramApiException {
        var chatId = getChatId(update);

        var helpText = """
            📋 Доступные команды:
            
            • Просто отправь любое сообщение - получишь эхо-ответ
            • Используй inline-кнопки для быстрого доступа
            
            🔄 Команды:
            /start - Начать работу
            /help - Получить справку
            /about - О боте
            
            Я всегда готов помочь! ✨
            """;

        var keyboard = createInlineKeyboard();
        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(helpText)
                .replyMarkup(keyboard)
                .build();

        absSender.execute(message);
        log.info("Sent help message to chat: {}", chatId);

    }

    @Override
    public String getCommandIdentifier() {
        return "help";
    }

    private InlineKeyboardMarkup createInlineKeyboard() {

        var aboutButton = InlineKeyboardButton.builder()
                .text("ℹ️ About")
                .callbackData("about")
                .build();

        var row = List.of(aboutButton);

        return InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();
    }
}
