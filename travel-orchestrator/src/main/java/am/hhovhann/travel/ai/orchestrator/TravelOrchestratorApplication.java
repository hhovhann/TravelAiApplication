package am.hhovhann.travel.ai.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai")
public class TravelOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelOrchestratorApplication.class, args);
    }
}
