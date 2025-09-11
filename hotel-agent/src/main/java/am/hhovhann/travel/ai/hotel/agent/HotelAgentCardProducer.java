package am.hhovhann.travel.ai.hotel.agent;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentInterface;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.TransportProtocol;
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

    @Bean("hotelAgentCard")
    @Primary
    public AgentCard hotelAgentCard() {
        return new AgentCard.Builder()
                .name("Hotel Agent")
                .description("AI agent for hotel search and booking via MCP servers")
                .url("http://localhost:" + serverPort + "/hotel")
                .preferredTransport(TransportProtocol.JSONRPC.asString())
                .additionalInterfaces(List.of(
                        new AgentInterface(TransportProtocol.JSONRPC.asString(), "http://localhost:" + serverPort + "/flight"),
                        new AgentInterface(TransportProtocol.GRPC.asString(), "http://localhost:" + serverPort + "/flight")
                ))
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
                .protocolVersion("0.3.0")
                .build();
    }
}
