package currency_tg_bot.demo.client;

import currency_tg_bot.demo.exception.ServiceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Component
public class CBRClient {
    private static final Logger LOG = LoggerFactory.getLogger(CBRClient.class);

    private final OkHttpClient client;
    private final String url;

    @Autowired
    public CBRClient(OkHttpClient client, @Value("${cbr.currency.rates.xml.url}") String url){
        this.client = client;
        this.url = url;
    }

    private void validate(String url) throws ServiceException {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            LOG.error("Неверный URL: {}", url, e);
            throw new ServiceException("Неверный URL:" + url, e);
        }
    }

    public String getCurrencyRatesXML() throws ServiceException {
        validate(url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ServiceException("XML не получен" + response);
            }
            String body = response.body().string();
            Optional<String> jsonResponse = Optional.ofNullable(body);
            return jsonResponse.orElseThrow(() -> new ServiceException("Получен пустой ответ"));
        } catch (IOException exception) {
            throw new ServiceException();
        }
    }
}
