package currency_tg_bot.demo.scheduler;

import currency_tg_bot.demo.bot.CurrencyBot;
import currency_tg_bot.demo.entity.SubscribedUsers;
import currency_tg_bot.demo.exception.ServiceException;
import currency_tg_bot.demo.repos.SubscribedUsersRepo;
import currency_tg_bot.demo.service.CoinMarketCapService;
import currency_tg_bot.demo.service.CurrencyBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
public class SubscriptionScheduler {
    @Autowired
    private SubscribedUsersRepo subscribedUsersRepo;

    @Autowired
    private CurrencyBotService currencyBotService;

    @Autowired
    private CoinMarketCapService coinMarketCapService;

    @Autowired
    CurrencyBot currencyBot;

    @Scheduled(fixedRate = 60000)
    public void sendDailyRates() throws ServiceException {
        var currentTime = LocalTime.now();
        var usdRate = currencyBotService.getUSDExchangeRate();
        var eurRate = currencyBotService.getEURExchangeRate();
        var btcRate = coinMarketCapService.getCryptoPrice("BTC");
        var ethRate = coinMarketCapService.getCryptoPrice("ETH");

        var message = String.format("""
                                           <b> Актуальные курсы на сегодня:
                                           $USD - %s
                                           $EUR - %s
                                           </b>""", usdRate, eurRate);

        for (SubscribedUsers subscribedUsers : subscribedUsersRepo.findAll()) {
            if (subscribedUsers.getNotificationTime() != null
                    && subscribedUsers.getNotificationTime()
                    .equals(currentTime.truncatedTo(ChronoUnit.MINUTES))) {
                        currencyBot.sendMessage(subscribedUsers.getChatId(),message);
            }
        }

    }
}
