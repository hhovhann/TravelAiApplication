package am.hhovhann.travel.ai.mcp.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "am.hhovhann.travel.ai")
public class McpFlightServerApplication {
    static void main(String[] args) {
        SpringApplication.run(McpFlightServerApplication.class, args);
    }
}
