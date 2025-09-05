package am.hhovhann.travel.ai.flight.agent;

import am.hhovhann.travel.ai.core.model.FlightRequest;
import am.hhovhann.travel.ai.core.model.FlightResponse;
import am.hhovhann.travel.ai.flight.service.FlightService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FlightAgent {
    private final FlightService flightService;
    private final ChatClient chatClient;

    @Autowired
    public FlightAgent(FlightService flightService, ChatClient.Builder chatClientBuilder) {
        this.flightService = flightService;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                You are a specialized Flight Agent that helps users find, compare, and book flights.
                You have access to flight search and booking capabilities through MCP servers.
                
                Your responsibilities:
                1. Understand user flight requirements and preferences
                2. Search for suitable flights using available tools
                3. Provide flight recommendations with explanations
                4. Coordinate with Hotel Agent when needed for complete travel planning
                5. Handle flight bookings and modifications
                
                Always be helpful, accurate, and provide clear explanations for your recommendations.
                When coordinating with other agents, be concise and specific about requirements.
                """)
                .build();
    }

    public String processFlightRequest(String userMessage) {
        try {
            String response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            // Extract flight requirements from the AI response
            // This is simplified - you'd want more sophisticated parsing
            if (userMessage.toLowerCase().contains("search") || userMessage.toLowerCase().contains("find")) {
                // Process as flight search
                return handleFlightSearch(userMessage, response);
            }

            return response;
        } catch (Exception e) {
            return "I apologize, but I encountered an error processing your flight request: " + e.getMessage();
        }
    }

    private String handleFlightSearch(String userMessage, String aiResponse) {
        // This would typically parse the user message to extract flight search parameters
        // For now, returning the AI response
        return aiResponse + "\n\nI can help you search for flights. Please provide your origin, destination, travel dates, and any preferences.";
    }

    public CompletableFuture<List<FlightResponse>> searchFlights(FlightRequest request) {
        return flightService.searchFlights(request);
    }

    public String coordinateWithHotelAgent(String flightDetails, String destination) {
        String coordinationMessage = String.format("""
            Flight coordination request:
            Destination: %s
            Flight Details: %s
            
            Please find suitable hotels near the destination airport or city center.
            """, destination, flightDetails);

        return chatClient.prompt()
                .user(coordinationMessage)
                .call()
                .content();
    }
}
