package currency_tg_bot.demo.repos;

import currency_tg_bot.demo.entity.SubscribedUsers;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface SubscribedUsersRepo extends JpaRepository<SubscribedUsers, Long> {
    @NotNull
    List<SubscribedUsers> findAll();
    boolean existsByChatId(Long chatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SubscribedUsers u WHERE u.chatId = ?1")
    void deleteByChatId(Long chatId);
    SubscribedUsers findByChatId(Long chatId);
}
