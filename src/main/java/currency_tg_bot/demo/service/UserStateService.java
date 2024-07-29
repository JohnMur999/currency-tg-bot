package currency_tg_bot.demo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStateService {
    private final Map<Long, String> userStates = new HashMap<>();

    public void setUserState(Long chatId, String state) {
        userStates.put(chatId, state);
    }

    public String getUserState(Long chatId) {
        return userStates.getOrDefault(chatId, "");
    }

    public void clearUserState(Long chatId) {
        userStates.remove(chatId);
    }
}
