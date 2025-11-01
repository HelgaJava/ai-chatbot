package ru.aialchemy.agent.bot.command.base;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class AbstractBotCommand implements BotCommand {

    @Override
    public abstract void executeCommand(AbsSender absSender, Update update) throws TelegramApiException;

    @Override
    public abstract String getCommandIdentifier();

    protected Long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }

    protected String getUsername(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChat().getUserName();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getUserName();
        }
        return null;
    }

}
