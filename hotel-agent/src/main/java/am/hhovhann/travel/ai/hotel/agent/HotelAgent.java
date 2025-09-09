package am.hhovhann.travel.ai.hotel.agent;

import am.hhovhann.travel.ai.core.model.HotelRequest;
import am.hhovhann.travel.ai.core.model.HotelResponse;
import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import am.hhovhann.travel.ai.hotel.service.HotelService;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

            // First, get the agent card for the A2A server agent you want to connect to
            AgentCard agentCard = new A2ACardResolver(flightAgentUrl).getAgentCard();

            // Specify configuration for the ClientBuilder
            ClientConfig clientConfig = new ClientConfig.Builder()
                    .setAcceptedOutputModes(List.of("text"))
                    .build();

            // Create event consumers to handle responses that will be received from the A2A server
            // (these consumers will be used for both streaming and non-streaming responses)
            List<BiConsumer<ClientEvent, AgentCard>> consumers = List.of(
                    (event, card) -> {
                        if (event instanceof MessageEvent messageEvent) {
                            // handle the messageEvent.getMessage()

                        } else if (event instanceof TaskEvent taskEvent) {
                            // handle the taskEvent.getTask()

                        } else if (event instanceof TaskUpdateEvent updateEvent) {
                            // handle the updateEvent.getTask()

                        }
                    }
            );

            // Create a handler that will be used for any errors that occur during streaming
            Consumer<Throwable> errorHandler = error -> {
                // handle the error.getMessage()
            };

            // Create the client using the builder
            Client flightAgentClient = Client
                    .builder(agentCard)
                    .clientConfig(clientConfig)
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .addConsumers(consumers)
                    .streamingErrorHandler(errorHandler)
                    .build();

            flightAgentClient.sendMessage(A2AMessageBuilder.createTextMessage(message));
            return "Coordinated with Flight Agent." ;
        } catch (Exception e) {
            return "Failed to communicate with Flight Agent: " + e.getMessage();
        }
    }

    public CompletableFuture<List<HotelResponse>> findHotelsNearFlightDestination(String destination, String airportCode) {
        return hotelService.findHotelsNearAirport(airportCode, null, null);
    }
}
