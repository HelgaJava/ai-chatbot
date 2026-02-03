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
            📋 Доступные действия:
            
            • Кнопка About - Краткая информация об AI-агенте.
            • Кнопка Help - Получить инструкцию по использованию бота.
            
            🔄 Инструкция по использованию AI-агента для аналитики и внесения изменений в Word-документы:
                     
            1. Прикрепите Word файл с помощью кнопки 📎 в строке для ввода сообщений.
            2. В поле Подпись добавьте задание для AI-агента. Например: Исправь пунктуационные ошибки в тексте, где идет речь про Машу.
            3. Нажмите кнопку Отправить и ждите результатов выполнения работы. По кончании своей работы AI-агент отправит вам уведомление.
            4. После вы можете открыть отредактированный файл и посмотреть результаты работы агента. Путь, по которому AI-агент сохранит изменения будет в ответном сообщении.
            5. Внесение изменений будет в следующем формате - AI-агент выделит участок текста, который нужно исправить жирным перечеркнутым шрифтом. На следующей AI-агент запишет новую редакцию.         
            
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
