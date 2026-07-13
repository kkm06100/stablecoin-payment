package stablecointransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StablecoinPaymentApplication {
  public static void main(String[] args) {
    SpringApplication.run(StablecoinPaymentApplication.class, args);
  }
}
