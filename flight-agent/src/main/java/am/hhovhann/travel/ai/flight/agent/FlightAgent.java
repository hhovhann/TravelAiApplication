package am.hhovhann.travel.ai.flight.agent;

import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import am.hhovhann.travel.ai.core.model.FlightRequest;
import am.hhovhann.travel.ai.core.model.FlightResponse;
import am.hhovhann.travel.ai.flight.service.FlightService;
//import io.a2a.client.A2AClient;
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
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
            // First, get the agent card for the A2A server agent you want to connect to
            AgentCard agentCard = new A2ACardResolver(hotelAgentUrl).getAgentCard();

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
            Client client = Client
                    .builder(agentCard)
                    .clientConfig(clientConfig)
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .addConsumers(consumers)
                    .streamingErrorHandler(errorHandler)
                    .build();

//            A2AClient hotelAgentClient = new A2AClient(hotelAgentUrl);

            String coordinationMessage = String.format("""
                    Flight coordination request:
                    Destination: %s
                    Flight Details: %s
                    
                    Please find suitable hotels near the destination airport or city center.
                    """, destination, flightDetails);

            client.sendMessage(A2AMessageBuilder.createTextMessage(coordinationMessage));
            return "Coordinated with Hotel Agent.";
        } catch (Exception e) {
            return "Failed to communicate with Hotel Agent: " + e.getMessage();
        }
    }
}
