package am.hhovhann.travel.ai.hotel.agent;

import am.hhovhann.travel.ai.core.model.HotelRequest;
import am.hhovhann.travel.ai.core.model.HotelResponse;
import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import am.hhovhann.travel.ai.hotel.service.HotelService;
import io.a2a.client.A2AClient;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class HotelAgent {
    private final HotelService hotelService;
    private final ChatClient chatClient;
    private final String flightAgentUrl;

    public HotelAgent(HotelService hotelService,
                      ChatClient chatClient,
                      @Value("${flight.agent.url:http://localhost:8080}") String flightAgentUrl) {
        this.hotelService = hotelService;
        this.chatClient = chatClient;
        this.flightAgentUrl = flightAgentUrl;
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
            A2AClient flightAgentClient = new A2AClient(flightAgentUrl);

            MessageSendParams params = new MessageSendParams.Builder()
                    .message(A2AMessageBuilder.createTextMessage(message))
                    .build();

            SendMessageResponse response = flightAgentClient.sendMessage(params);
            return "Coordinated with Flight Agent. Task ID: " + response.getId();
        } catch (Exception e) {
            return "Failed to communicate with Flight Agent: " + e.getMessage();
        }
    }

    public CompletableFuture<List<HotelResponse>> findHotelsNearFlightDestination(String destination, String airportCode) {
        return hotelService.findHotelsNearAirport(airportCode, null, null);
    }
}
