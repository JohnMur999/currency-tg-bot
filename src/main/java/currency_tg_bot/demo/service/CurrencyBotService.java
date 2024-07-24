package currency_tg_bot.demo.service;

import currency_tg_bot.demo.exception.ServiceException;

public interface CurrencyBotService {
    String getUSDExchangeRate() throws ServiceException;
    String getEURExchangeRate() throws ServiceException;
}
