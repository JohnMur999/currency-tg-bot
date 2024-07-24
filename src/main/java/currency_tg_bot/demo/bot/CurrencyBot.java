package currency_tg_bot.demo.bot;

import currency_tg_bot.demo.exception.ServiceException;
import currency_tg_bot.demo.service.CurrencyBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;

@Component
public class CurrencyBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyBot.class);

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String HELP = "/help";

    @Autowired
    public CurrencyBotService currencyBotService;

    public CurrencyBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage());
        }
    }

    private void startCommand(Long chatId, String username) {
        var messageForUser = """
                Привет, %s!
                                          
                Доступные команды:
                /usd - курс доллара;
                /eur - курс евро;
                                          
                /help - справка
                    
                """;
        var formattedMessageForUser = String.format(messageForUser, username);
        sendMessage(chatId, formattedMessageForUser);
    }

    private void usdCommand(Long chatId, String username) {
        LOG.info("{} used USD command in chat {}", username, chatId);
        String formattedMessageForUser;
        try {
            var eur = currencyBotService.getEURExchangeRate();
            var message = "$USD = %s | Дата: %s";
            formattedMessageForUser = String.format(message, eur, LocalDate.now());
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
        sendMessage(chatId, formattedMessageForUser);
    }

    private void eurCommand(Long chatId, String username) {
        LOG.info("{} used EUR command in chat {}", username, chatId);
        String formattedMessageForUser;
        try {
            var usd = currencyBotService.getUSDExchangeRate();
            LOG.info(usd.toString());
            var message = "$EUR = %s | Дата: %s";
            formattedMessageForUser = String.format(message,usd, LocalDate.now());
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
        sendMessage(chatId, formattedMessageForUser);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var chatId = message.getChatId();

            switch (message.getText()) {
                case START -> {
                    String username = update.getMessage().getFrom().getUserName();
                    startCommand(chatId, username);
                    LOG.info("{} used START command in chat {}", username, chatId);
                }
                case USD -> {
                    String username = update.getMessage().getFrom().getUserName();
                    usdCommand(chatId, username);
                }
                case EUR -> {
                    String username = update.getMessage().getFrom().getUserName();
                    eurCommand(chatId, username);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "currency999price999bot";
    }
}
