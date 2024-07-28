package currency_tg_bot.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalTime;

@Entity
public class SubscribedUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;
    private String username;
    private LocalTime notificationTime;


    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(LocalTime notificationTime) {
        this.notificationTime = notificationTime;
    }
}

