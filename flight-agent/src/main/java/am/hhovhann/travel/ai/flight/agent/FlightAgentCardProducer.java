package am.hhovhann.travel.ai.flight.agent;

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
public class FlightAgentCardProducer {

    @Value("${server.port:8080}")
    private String serverPort;

    // Agent Card Bean - A2A SDK will automatically expose this
    @Bean
    @Primary
    public AgentCard flightAgentCard() {
        return new AgentCard.Builder()
                .name("Flight Agent")
                .description("AI agent for flight search and booking via MCP servers")
                .url("http://localhost:" + serverPort)
                .version("1.0.0")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(false)
                        .stateTransitionHistory(true)
                        .build())
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(List.of(
                        new AgentSkill.Builder()
                                .id("flight_search")
                                .name("Flight Search")
                                .description("Search flights via MCP server integration")
                                .tags(List.of("flights", "search", "mcp"))
                                .examples(List.of("Find flights from NYC to London"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("flight_booking")
                                .name("Flight Booking")
                                .description("Book flights via MCP server integration")
                                .tags(List.of("flights", "booking", "mcp"))
                                .examples(List.of("Book flight for 2 passengers"))
                                .build()
                ))
                .protocolVersion("0.2.5")
                .build();
    }
}
