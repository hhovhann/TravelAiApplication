package am.hhovhann.travel.ai.mcp.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai")
public class McpHotelServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpHotelServerApplication.class, args);
    }
}
