package am.hhovhann.travel.ai.hotel.config;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;

@Configuration
public class HotelAgentCardProducer {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    @Primary
    public AgentCard hotelAgentCard() {
        return new AgentCard.Builder()
                .name("Hotel Agent")
                .description("AI agent for hotel search and booking via MCP servers")
                .url("http://localhost:" + serverPort + "/.well-known/agent.json")
                .version("1.0.0")
                .protocolVersion("2.5")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(false)
                        .stateTransitionHistory(true)
                        .build())
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(List.of(
                        new AgentSkill.Builder()
                                .id("hotel_search")
                                .name("Hotel Search")
                                .description("Search hotels via MCP server integration")
                                .tags(List.of("hotels", "search", "mcp"))
                                .examples(List.of("Find hotels in Paris"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("hotel_booking")
                                .name("Hotel Booking")
                                .description("Book hotels via MCP server integration")
                                .tags(List.of("hotels", "booking", "mcp"))
                                .examples(List.of("Book hotel room for 3 nights"))
                                .build()
                ))
                .build();
    }
}
