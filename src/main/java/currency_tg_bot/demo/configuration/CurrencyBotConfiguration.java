package currency_tg_bot.demo.configuration;

import currency_tg_bot.demo.bot.CurrencyBot;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class CurrencyBotConfiguration {
    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        return options;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
    @Bean
    public TelegramBotsApi telegramBotsApi(CurrencyBot currencyBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(currencyBot);
        return api;
    }
}
