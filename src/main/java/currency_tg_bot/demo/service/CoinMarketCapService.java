package currency_tg_bot.demo.service;

import currency_tg_bot.demo.exception.ServiceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CoinMarketCapService {

    private final OkHttpClient client;
    private final String apiKey;
    private final String apiUrl;

    public CoinMarketCapService(OkHttpClient client, @Value("${cmc.api.key}") String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
        this.apiUrl = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest";
    }
    public String getCryptoPrice(String symbol) throws ServiceException {
        Request request = new Request.Builder()
                .url(apiUrl + "?symbol=" + symbol)
                .addHeader("X-CMC_PRO_API_KEY", apiKey)
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Optional<String> jsonResponse = Optional.ofNullable(body);
            return jsonResponse.orElseThrow(() -> new ServiceException());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
