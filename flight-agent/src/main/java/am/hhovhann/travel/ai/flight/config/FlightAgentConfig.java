package am.hhovhann.travel.ai.flight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FlightAgentConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
