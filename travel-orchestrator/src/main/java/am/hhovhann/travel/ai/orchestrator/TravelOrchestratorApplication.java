package am.hhovhann.travel.ai.orchestrator;

import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai", exclude = {ToolCallingAutoConfiguration.class})
public class TravelOrchestratorApplication {
    static void main(String[] args) {
        SpringApplication.run(TravelOrchestratorApplication.class, args);
    }
}
