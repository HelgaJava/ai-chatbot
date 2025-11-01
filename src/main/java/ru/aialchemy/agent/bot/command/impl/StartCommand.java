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
public class StartCommand extends AbstractBotCommand {

    @Override
    public void executeCommand(AbsSender absSender, Update update) throws TelegramApiException {
        var chatId = getChatId(update);
        var userName = getUsername(update);

        var greeting = (userName != null) ?
                "Привет, @%s! 👋".formatted(userName) :
                "Привет! 👋";

        var responseText = """
                %s
                            
                Я готов к работе! Просто отправь мне любое сообщение, и я отвечу эхом.
                            
                Используй кнопки ниже для навигации.
                """.formatted(greeting);

        var keyboard = createInlineKeyboard();
        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(responseText)
                .replyMarkup(keyboard)
                .build();

        absSender.execute(message);
        log.info("Sent start message to chat: {}", chatId);

    }

    @Override
    public String getCommandIdentifier() {
        return "start";
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

        return InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();
    }
}
