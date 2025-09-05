package am.hhovhann.travel.ai.flight;

import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai", exclude = {ToolCallingAutoConfiguration.class})
public class FlightAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightAgentApplication.class, args);
    }
}
