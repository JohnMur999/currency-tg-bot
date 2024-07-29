package currency_tg_bot.demo.service.impl;

import currency_tg_bot.demo.client.CBRClient;
import currency_tg_bot.demo.exception.ServiceException;
import currency_tg_bot.demo.service.CoinMarketCapService;
import currency_tg_bot.demo.service.CurrencyBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Optional;

@Service
public class CurrencyBotServiceImpl implements CurrencyBotService {
    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";

    private final CBRClient cbrClient;
    private final CoinMarketCapService coinMarketCapService;

    @Autowired
    public CurrencyBotServiceImpl(CBRClient client, CoinMarketCapService coinMarketCapService) {
        this.cbrClient = client;
        this.coinMarketCapService = coinMarketCapService;
    }

    @Override
    public String getUSDExchangeRate() throws ServiceException {
        Optional<String> xmlOptional = Optional.ofNullable(cbrClient.getCurrencyRatesXML());
        String xml = xmlOptional.orElseThrow(
                ServiceException::new
        );
        return extractCurrencyValuesFromXML(xml, USD_XPATH);
    }

    @Override
    public String getEURExchangeRate() throws ServiceException {
        var xml = cbrClient.getCurrencyRatesXML();
        return extractCurrencyValuesFromXML(xml, EUR_XPATH);
    }

    @Override
    public String getCryptoPrice(String cryptoTokenName) throws ServiceException {
        return coinMarketCapService.getCryptoPrice(cryptoTokenName);
    }

    private static String extractCurrencyValuesFromXML(String xml, String xpathExpression) throws ServiceException {
        var source = new InputSource(new StringReader(xml));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var document = (Document) xpath.evaluate("/", source, XPathConstants.NODE);
            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException e) {
            throw new ServiceException();
        }
    }
}
