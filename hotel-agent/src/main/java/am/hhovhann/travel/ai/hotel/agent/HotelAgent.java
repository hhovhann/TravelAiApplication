package am.hhovhann.travel.ai.hotel.agent;

import am.hhovhann.travel.ai.core.model.HotelRequest;
import am.hhovhann.travel.ai.core.model.HotelResponse;
import am.hhovhann.travel.ai.hotel.service.HotelService;
import io.a2a.sdk.client.A2AClient;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class HotelAgent {
    private final HotelService hotelService;
    private final ChatClient chatClient;
    private final A2AClient flightAgentClient;

    @Autowired
    public HotelAgent(HotelService hotelService,
                      ChatClient.Builder chatClientBuilder,
                      @Value("${flight.agent.url:http://localhost:8080}") String flightAgentUrl) {
        this.hotelService = hotelService;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                You are a specialized Hotel Agent that helps users find, compare, and book accommodations.
                You have access to hotel search and booking capabilities through MCP servers.
                
                Your responsibilities:
                1. Understand user hotel requirements and preferences
                2. Search for suitable hotels using available tools
                3. Provide hotel recommendations with detailed explanations
                4. Coordinate with Flight Agent when needed for complete travel planning
                5. Handle hotel bookings and modifications
                6. Consider proximity to airports, attractions, and transportation
                
                Always be helpful, accurate, and provide clear explanations for your recommendations.
                When coordinating with other agents, be concise and specific about requirements.
                """)
                .build();
        this.flightAgentClient = new A2AClient(flightAgentUrl);
    }

    public String processHotelRequest(String userMessage) {
        try {
            String response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            if (userMessage.toLowerCase().contains("search") || userMessage.toLowerCase().contains("find")) {
                return handleHotelSearch(userMessage, response);
            }

            return response;
        } catch (Exception e) {
            return "I apologize, but I encountered an error processing your hotel request: " + e.getMessage();
        }
    }

    private String handleHotelSearch(String userMessage, String aiResponse) {
        return aiResponse + "\n\nI can help you search for hotels. Please provide your destination, check-in/check-out dates, number of guests, and any preferences.";
    }

    public CompletableFuture<List<HotelResponse>> searchHotels(HotelRequest request) {
        return hotelService.searchHotels(request);
    }

    public String communicateWithFlightAgent(String message) {
        try {
            Message agentMessage = new Message.Builder()
                    .addTextPart(message)
                    .build();

            MessageSendParams params = new MessageSendParams.Builder()
                    .message(agentMessage)
                    .build();

            SendMessageResponse response = flightAgentClient.sendMessage(params);
            return "Coordinated with Flight Agent. Task ID: " + response.getTask().getId();
        } catch (Exception e) {
            return "Failed to communicate with Flight Agent: " + e.getMessage();
        }
    }

    public CompletableFuture<List<HotelResponse>> findHotelsNearFlightDestination(String destination, String airportCode) {
        return hotelService.findHotelsNearAirport(airportCode, null, null);
    }
}
