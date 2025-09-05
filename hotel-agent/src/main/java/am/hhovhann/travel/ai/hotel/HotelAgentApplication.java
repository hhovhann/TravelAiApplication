package am.hhovhann.travel.ai.hotel;

import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai", exclude = {ToolCallingAutoConfiguration.class})
public class HotelAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelAgentApplication.class, args);
    }
}
