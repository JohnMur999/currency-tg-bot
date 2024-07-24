package currency_tg_bot.demo.service.impl;

import currency_tg_bot.demo.client.CBRClient;
import currency_tg_bot.demo.exception.ServiceException;
import currency_tg_bot.demo.service.CurrencyBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

import currency_tg_bot.demo.client.CBRClient;
import currency_tg_bot.demo.exception.ServiceException;
import currency_tg_bot.demo.service.CurrencyBotService;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Optional;

@Service
public class CurrencyBotServiceImpl implements CurrencyBotService {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyBotServiceImpl.class);

    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";

    @Autowired
    private CBRClient client;

    public CurrencyBotServiceImpl(CBRClient client) {
        this.client = client;
    }

    @Override
    public String getUSDExchangeRate() throws ServiceException {
        Optional<String> xmlOptional = Optional.ofNullable(client.getCurrencyRatesXML());
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException()
        );
        return extractCurrencyValuesFromXML(xml, USD_XPATH);
    }

    @Override
    public String getEURExchangeRate() throws ServiceException {
        var xml = client.getCurrencyRatesXML();
        return extractCurrencyValuesFromXML(xml, EUR_XPATH);
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
