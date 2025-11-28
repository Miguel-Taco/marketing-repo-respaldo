package pe.unmsm.crm.marketing;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class MarketingCrmApplication {

    static {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(MarketingCrmApplication.class, args);
    }
}
