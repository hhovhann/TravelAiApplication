package am.hhovhann.travel.ai.flight.agent;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.PublicAgentCard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class FlightAgentCardProducer {

    @Produces
    @PublicAgentCard
    public AgentCard agentCard() {
        return new AgentCard.Builder()
                .name("Flight Agent")
                .description("Specialized agent for flight search, booking, and recommendations")
                .url("http://localhost:8080")
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
                                .description("Search for flights based on origin, destination, dates, and preferences")
                                .tags(List.of("flights", "search", "travel"))
                                .examples(List.of("Find flights from NYC to London on December 15th"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("flight_booking")
                                .name("Flight Booking")
                                .description("Book flights and manage reservations")
                                .tags(List.of("flights", "booking", "reservation"))
                                .examples(List.of("Book flight BA123 for 2 passengers"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("flight_recommendations")
                                .name("Flight Recommendations")
                                .description("Provide flight recommendations based on preferences and travel patterns")
                                .tags(List.of("flights", "recommendations", "ai"))
                                .examples(List.of("Recommend best flights to Paris for business travel"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("hotel_coordination")
                                .name("Hotel Coordination")
                                .description("Coordinate with hotel agent for complete travel planning")
                                .tags(List.of("coordination", "hotel", "integration"))
                                .examples(List.of("Find hotels near flight destination"))
                                .build()
                ))
                .build();
    }
}
