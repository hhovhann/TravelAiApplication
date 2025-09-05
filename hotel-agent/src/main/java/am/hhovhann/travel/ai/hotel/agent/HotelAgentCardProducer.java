package am.hhovhann.travel.ai.hotel.agent;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.PublicAgentCard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class HotelAgentCardProducer {

    @Produces
    @PublicAgentCard
    public AgentCard agentCard() {
        return new AgentCard.Builder()
                .name("Hotel Agent")
                .description("Specialized agent for hotel search, booking, and accommodation recommendations")
                .url("http://localhost:8082")
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
                                .description("Search for hotels based on destination, dates, and preferences")
                                .tags(List.of("hotels", "search", "accommodation"))
                                .examples(List.of("Find hotels in Paris for December 15-20"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("hotel_booking")
                                .name("Hotel Booking")
                                .description("Book hotel rooms and manage reservations")
                                .tags(List.of("hotels", "booking", "reservation"))
                                .examples(List.of("Book room at Marriott Hotel for 3 nights"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("hotel_recommendations")
                                .name("Hotel Recommendations")
                                .description("Provide hotel recommendations based on preferences and location")
                                .tags(List.of("hotels", "recommendations", "ai"))
                                .examples(List.of("Recommend luxury hotels in downtown London"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("airport_proximity")
                                .name("Airport Proximity Search")
                                .description("Find hotels near airports or transportation hubs")
                                .tags(List.of("hotels", "airport", "proximity"))
                                .examples(List.of("Find hotels near JFK airport"))
                                .build()
                ))
                .build();
    }
}
