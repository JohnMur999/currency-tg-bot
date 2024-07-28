package currency_tg_bot.demo.repos;

import currency_tg_bot.demo.entity.SubscribedUsers;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscribedUsersRepo extends JpaRepository<SubscribedUsers, Long> {
    @NotNull
    List<SubscribedUsers> findAll();
    boolean existsByChatId(Long chatId);
    void deleteByChatId(Long chatId);
    SubscribedUsers findByChatId(Long chatId);
}
