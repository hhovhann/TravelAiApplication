package am.hhovhann.travel.ai.flight.agent;

import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import am.hhovhann.travel.ai.core.model.FlightRequest;
import am.hhovhann.travel.ai.core.model.FlightResponse;
import am.hhovhann.travel.ai.flight.service.FlightService;
import io.a2a.client.A2AClient;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FlightAgent {
    private final FlightService flightService;
    private final ChatClient chatClient;
    private final String hotelAgentUrl;

    public FlightAgent(FlightService flightService,
                       ChatClient chatClient,
                       @Value("${hotel.agent.url:http://localhost:8082}") String hotelAgentUrl) {
        this.flightService = flightService;
        this.chatClient = chatClient;
        this.hotelAgentUrl = hotelAgentUrl;
    }

    public String processFlightRequest(String userMessage) {
        try {
            String response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            if (userMessage.toLowerCase().contains("search") || userMessage.toLowerCase().contains("find")) {
                return handleFlightSearch(userMessage, response);
            }

            return response;
        } catch (Exception e) {
            return "I apologize, but I encountered an error processing your flight request: " + e.getMessage();
        }
    }

    private String handleFlightSearch(String userMessage, String aiResponse) {
        return aiResponse + "\n\nI can help you search for flights. Please provide your origin, destination, travel dates, and any preferences.";
    }

    public CompletableFuture<List<FlightResponse>> searchFlights(FlightRequest request) {
        return flightService.searchFlights(request);
    }

    public String communicateWithHotelAgent(String flightDetails, String destination) {
        try {
            A2AClient hotelAgentClient = new A2AClient(hotelAgentUrl);

            String coordinationMessage = String.format("""
                    Flight coordination request:
                    Destination: %s
                    Flight Details: %s
                    
                    Please find suitable hotels near the destination airport or city center.
                    """, destination, flightDetails);

            MessageSendParams params = new MessageSendParams.Builder()
                    .message(A2AMessageBuilder.createTextMessage(coordinationMessage))
                    .build();

            SendMessageResponse response = hotelAgentClient.sendMessage(params);
            return "Coordinated with Hotel Agent. Task ID: " + response.getId();
        } catch (Exception e) {
            return "Failed to communicate with Hotel Agent: " + e.getMessage();
        }
    }
}
