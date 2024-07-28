package currency_tg_bot.demo.bot;

import currency_tg_bot.demo.entity.SubscribedUsers;
import currency_tg_bot.demo.exception.ServiceException;
import currency_tg_bot.demo.repos.SubscribedUsersRepo;
import currency_tg_bot.demo.service.CurrencyBotService;
import currency_tg_bot.demo.service.UserStateService;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class CurrencyBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyBot.class);

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String HELP = "/help";
    private static final String FINDCRYPTO = "/findcrypto";
    private static final String TODAY = "/today";
    private static final String SUB = "/sub";
    private static final String UNSUB = "/unsub";
    private static final String CHANGESUBTIME = "/changesubtime";

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @Autowired
    public CurrencyBotService currencyBotService;
    @Autowired
    private UserStateService userStateService;

    @Autowired
    public CurrencyBot(DefaultBotOptions options,
                       @Value("${bot.token}") String botToken,
                       @Value("${bot.username}") String botUsername) {
        super(options);
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        sendMessage.setParseMode("HTML");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage());
        }
    }

    private void startCommand(Long chatId, String username) {
        var messageForUser = """
                <b> 👋 Привет, %s!
                
                В боте доступны следующие команды:
                
                📊 Актуальные валютные курсы:
                /usd - доллара США;
                /eur - евро;
                /findcrypto - курс криптовалюты;
                
                📈📉 Валютная сводка:
                /today - узнать актуальные цены основных валют;
                
                📆 Подписки:
                /sub - подписаться на ежедневную рассылку;
                /changesubtime - изменить время рассылки;
                /unsub - отписаться от рассылки;
                
                🆘 Помощь с командами:
                /help - справка. </b>
                """;
        var formattedMessageForUser = String.format(messageForUser, username);
        sendMessage(chatId, formattedMessageForUser);
    }

    @Autowired
    private SubscribedUsersRepo subscribedUsersRepo;

    @Scheduled(cron = "0 0 * * * *")
    public void sendDailyUpdates() {
        List<SubscribedUsers> users = subscribedUsersRepo.findAll();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        for (SubscribedUsers user : users) {
            LocalTime notificationTime = user.getNotificationTime();
            if (notificationTime != null && notificationTime.equals(now)) {
                Long chatId = user.getChatId();
                String username = user.getUsername();
                usdCommand(chatId, username);
                eurCommand(chatId, username);
            }
        }
    }

    private void subscribeCommand(Long chatId, String username) {
        if (!subscribedUsersRepo.existsByChatId(chatId)) {
            SubscribedUsers subscribedUsers = new SubscribedUsers();
            subscribedUsers.setChatId(chatId);
            subscribedUsers.setUsername(username);
            subscribedUsersRepo.save(subscribedUsers);
            String subMessage = "<b> Вы успешно подписались на рассылку! Осталось лишь выбрать время для полечения рассылки." +
                    "\n\nИспользуйте /changesubtime </b>";
            sendMessage(chatId,subMessage);
        } else {
            String message = "<b> Вы уже подписаны на рассылку </b>";
            sendMessage(chatId,message);
        }
    }

    private void setTimeCommand(Long chatId, String time) {
        try {
            LocalTime notificationTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"));
            if (subscribedUsersRepo.existsByChatId(chatId)) {
                SubscribedUsers subscribedUsers = subscribedUsersRepo.findByChatId(chatId);
                subscribedUsers.setNotificationTime(notificationTime);
                subscribedUsersRepo.save(subscribedUsers);

                LocalTime now = LocalTime.now();
                long minutesUntilNextNotification = now.until(notificationTime, ChronoUnit.MINUTES);
                if (minutesUntilNextNotification < 0) {
                    minutesUntilNextNotification += 24 * 60;
                }
                long hoursUntilNextNotification = minutesUntilNextNotification / 60;
                long remainingMinutes = minutesUntilNextNotification % 60;

                String message = String.format(
                        "<b>Время подписки изменено на</b> %s.<b>\n\nВы получите следующую рассылку через %d часов и %d минут.</b>",
                        time, hoursUntilNextNotification, remainingMinutes);
                sendMessage(chatId, message);
            } else {
                String message = "<b> Вы не подписаны на рассылку </b>";
                sendMessage(chatId,message);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            sendMessage(chatId, "<b>Неправильный формат времени. Попробуйте еще раз.</b>" +
                    "<b>\n\nПожалуйста, вводите время в формате HH:MM (00:00 - 23:59).</b>");

        }

    }

    private void unsubscribeCommand(Long chatId, String username) {
        if (!subscribedUsersRepo.existsByChatId(chatId)) {
            String message = "<b> Вы не подписаны на рассылку </b>";
            sendMessage(chatId,message);
        } else {
            subscribedUsersRepo.deleteByChatId(chatId);
            String message = "<b> Вы успешно отписались от рассылки </b>";
            sendMessage(chatId,message);
        }
    }

    private void usdCommand(Long chatId, String username) {
        LOG.info("{} used USD command in chat {}", username, chatId);
        String formattedMessageForUser;
        try {
            var eur = currencyBotService.getEURExchangeRate();
            var message = "<b> $USD = ₽%s | \uD83D\uDCC5: %s| via ЦБ РФ </b>";
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
            var message = "<b> $EUR = ₽%s | \uD83D\uDCC5: %s | via ЦБ РФ </b>";
            formattedMessageForUser = String.format(message, usd, LocalDate.now());
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
        sendMessage(chatId, formattedMessageForUser);
    }

    private void findCryptoCommand(Long chatId, String username) {
        LOG.info("{} used findcrypto command in chat {}", username, chatId);
        sendCryptoOptions(chatId);
        userStateService.setUserState(chatId, "AWAITING_CRYPTO_NAME");;
    }

    private void sendCryptoOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("<b> <u> Введите</u> или <u>выберите</u> уникальный модификатор монеты (BTC,ETH,NEAR...): </b>");
        message.setParseMode("HTML");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = getLists();

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static @NotNull List<List<InlineKeyboardButton>> getLists() {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton btcButton = new InlineKeyboardButton();
        btcButton.setText("Bitcoin");
        btcButton.setCallbackData("BTC");
        rowInline1.add(btcButton);


        InlineKeyboardButton ethButton = new InlineKeyboardButton();
        ethButton.setText("Ethereum");
        ethButton.setCallbackData("ETH");
        rowInline1.add(ethButton);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton ltcButton = new InlineKeyboardButton();
        ltcButton.setText("Litecoin");
        ltcButton.setCallbackData("LTC");
        rowInline2.add(ltcButton);

        InlineKeyboardButton xrpButton = new InlineKeyboardButton();
        xrpButton.setText("Ripple");
        xrpButton.setCallbackData("XRP");
        rowInline2.add(xrpButton);

        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        return rowsInline;
    }

    private void handleCryptoCommand(Long chatId, String cryptoName) {
        userStateService.clearUserState(chatId);
        String formattedMessageForUser;
        try {
            var cryptoToken = currencyBotService.getCryptoPrice(cryptoName);
            var message = "<b> $%s = $ %s | \uD83D\uDCC5: %s | CoinMarketCap </b>";
            formattedMessageForUser = String.format(message, cryptoName,
                    extractPriceFromCryptoResponse(cryptoToken, cryptoName),
                    LocalDate.now());
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
        sendMessage(chatId, formattedMessageForUser);
    }

    private String extractPriceFromCryptoResponse(String response, String cryptoName) {
        try {
            var json = new JSONObject(response);
            if (json.has("data") && json.getJSONObject("data").has(cryptoName)) {
                double price = json.getJSONObject("data")
                        .getJSONObject(cryptoName)
                        .getJSONObject("quote")
                        .getJSONObject("USD")
                        .getDouble("price");
                return String.valueOf(price);

            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return "Не найдено";
    }

    private void helpCommand(Long chatId, String username) {
        LOG.info("{} used HELP command in chat {}", username, chatId);
        var message = """
                <b>Доступные команды:
               📊 Актуальные валютные курсы:
                /usd - доллара США;
                /eur - евро;
                /findcrypto - курс криптовалюты;
                
                📈📉 Валютная сводка:
                /today - узнать актуальные цены основных валют;
                
                📆 Подписки:
                /sub - подписаться на ежедневную рассылку;
                /changesubtime - изменить время рассылки;
                /unsub - отписаться от рассылки;
                </b>""";
        sendMessage(chatId, message);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var chatId = message.getChatId();
            var textFromUser = message.getText();
            var userState = userStateService.getUserState(chatId);

            if ("AWAITING_CRYPTO_NAME".equals(userState)) {
                handleCryptoCommand(chatId, textFromUser.toUpperCase());
            } else if ("AWAITING_SUB_TIME".equals(userState)) {
                setTimeCommand(chatId, textFromUser);
                userStateService.clearUserState(chatId);
            }
            else {
                switch (textFromUser) {
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
                    case TODAY -> {
                        String username = update.getMessage().getFrom().getUserName();

                    }
                    case SUB -> {
                        String username = update.getMessage().getFrom().getUserName();
                        subscribeCommand(chatId, username);
                    }
                    case CHANGESUBTIME -> {
                        String username = update.getMessage().getFrom().getUserName();
                        if (subscribedUsersRepo.findByChatId(chatId).getNotificationTime() != null){
                            String currentSubTime = "<b>Текущее время подписки: </b>" + subscribedUsersRepo.findByChatId(chatId).getNotificationTime().toString();
                            sendMessage(chatId, currentSubTime);
                        }
                        String subMessage = "<b> Введите время для получения рассылки [HH:MM (00:00 - 23:59)] </b>";
                        sendMessage(chatId, subMessage);
                        userStateService.setUserState(chatId, "AWAITING_SUB_TIME");;
                    }
                    case UNSUB -> {
                        String username = update.getMessage().getFrom().getUserName();
                        unsubscribeCommand(chatId, username);
                    }
                    case HELP -> {
                        String username = update.getMessage().getFrom().getUserName();
                        helpCommand(chatId, username);
                    }
                    case FINDCRYPTO -> {
                        String username = update.getMessage().getFrom().getUserName();
                        findCryptoCommand(chatId, username);
                    }
                    default -> {
                        String errorMessage = "<b>Неизвестная команда, попробуйте еще раз</b>";
                        sendMessage(chatId, errorMessage);
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            handleCryptoCommand(chatId, callbackData);
        }
    }
}
