package ru.aialchemy.agent.bot.command.base;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotCommand {

    void executeCommand(AbsSender absSender, Update update) throws TelegramApiException;
    String getCommandIdentifier();
}
