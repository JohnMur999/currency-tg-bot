package currency_tg_bot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CurrencyTGBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyTGBotApplication.class, args);
	}

}
