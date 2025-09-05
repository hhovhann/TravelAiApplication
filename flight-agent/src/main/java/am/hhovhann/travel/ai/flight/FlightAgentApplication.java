package am.hhovhann.travel.ai.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai")
public class FlightAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightAgentApplication.class, args);
    }
}
