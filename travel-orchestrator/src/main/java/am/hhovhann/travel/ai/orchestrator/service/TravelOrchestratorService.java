package am.hhovhann.travel.ai.orchestrator.service;

import am.hhovhann.travel.ai.core.model.TravelPlan;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.JdkA2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.client.transport.spi.interceptors.ClientCallContext;
import io.a2a.spec.A2AClientError;
import io.a2a.spec.A2AClientException;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.springframework.ai.model.ModelOptionsUtils.OBJECT_MAPPER;

@Service
public class TravelOrchestratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TravelOrchestratorService.class);


    private final ChatClient chatClient;
    private final String flightAgentUrl;
    private final String hotelAgentUrl;
    private Client flightAgentClient;
    private Client hotelAgentClient;

    public TravelOrchestratorService(
            ChatClient.Builder chatClientBuilder,
            @Value("${agents.flight.url:http://localhost:8080/}") String flightAgentUrl,
            @Value("${agents.hotel.url:http://localhost:8082/}") String hotelAgentUrl) {
        this.flightAgentUrl = flightAgentUrl;
        this.hotelAgentUrl = hotelAgentUrl;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are a Travel Orchestrator that coordinates between Flight and Hotel agents to create comprehensive travel plans.
                        
                        Your responsibilities:
                        1. Understand user travel requirements comprehensively
                        2. Coordinate with Flight Agent for flight arrangements via A2A protocol
                        3. Coordinate with Hotel Agent for accommodation arrangements via A2A protocol
                        4. Create cohesive travel plans that optimize the entire journey
                        5. Handle complex multi-agent conversations and dependencies
                        
                        You communicate with specialized agents using the Agent-to-Agent (A2A) protocol.
                        Always provide clear, comprehensive travel recommendations.
                        """)
                .build();
    }

    // Common client initialization method
    private Client initializeAgentClient(String agentType, String agentUrl, String agentCardPath) {
        try {
            AgentCard agentCard = new A2ACardResolver(
                    new JdkA2AHttpClient(),
                    agentUrl,
                    agentCardPath
            ).getAgentCard();

            ClientConfig clientConfig = new ClientConfig.Builder()
                    .setAcceptedOutputModes(List.of("text"))
                    .setStreaming(false)
                    .build();

            return Client.builder(agentCard)
                    .clientConfig(clientConfig)
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + agentType + " agent client: " + e.getMessage(), e);
        }
    }

    private synchronized Client getFlightAgentClient() {
        if (flightAgentClient == null) {
            flightAgentClient = initializeAgentClient(
                    "flight",
                    flightAgentUrl,
                    flightAgentUrl + "/.well-known/agent-card.json"
            );
        }
        return flightAgentClient;
    }

    private synchronized Client getHotelAgentClient() {
        if (hotelAgentClient == null) {
            hotelAgentClient = initializeAgentClient(
                    "hotel",
                    hotelAgentUrl,
                    hotelAgentUrl + "/.well-known/agent-card.json"
            );
        }
        return hotelAgentClient;
    }

    // Improved send message method that properly handles responses
    private CompletableFuture<String> sendA2AMessage(Client client, String messageText) {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        Message request = A2A.toUserMessage(messageText);
        ClientCallContext clientCallContext = new ClientCallContext(new HashMap<>(), new HashMap<>());
        Consumer<Throwable> throwableConsumer = (Throwable error) -> {
            LOGGER.error("Streaming error occurred: {}", error.getMessage());
            responseFuture.completeExceptionally(error);
        };

        BiConsumer<ClientEvent, AgentCard> biConsumer = (event, card) -> {
            if (event instanceof MessageEvent messageEvent) {
                StringBuilder textBuilder = new StringBuilder();
                for (Part<?> part : messageEvent.getMessage().getParts()) {
                    if (part instanceof TextPart textPart) {
                        textBuilder.append(textPart.getText());
                    }
                }
                responseFuture.complete(textBuilder.toString());
            } else if (event instanceof TaskEvent taskEvent) {
                responseFuture.complete("Task created: " + taskEvent.getTask().getId());
            } else if (event instanceof TaskUpdateEvent updateEvent) {
                responseFuture.complete("Task updated: " + updateEvent.getTask().getId());
            }
        };

        try {
            client.sendMessage(request, List.of(biConsumer), throwableConsumer, clientCallContext);
        } catch (Exception e) {
            responseFuture.completeExceptionally(e);
        }

        return responseFuture;
    }

    public CompletableFuture<TravelPlan> planTrip(Map<String, Object> travelRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String tripId = UUID.randomUUID().toString();
                String from = (String) travelRequest.get("from");
                String to = (String) travelRequest.get("to");
                String departureDate = (String) travelRequest.get("departureDate");
                String returnDate = (String) travelRequest.get("returnDate");
                String preferences = (String) travelRequest.get("preferences");
                Integer passengers = (Integer) travelRequest.get("passengers");

                // Create coordination plan
                String coordinationPlan = chatClient.prompt()
                        .user(String.format("""
                                        Create a travel coordination plan for:
                                        - Trip: %s to %s
                                        - Departure: %s, Return: %s
                                        - Passengers: %d
                                        - Preferences: %s
                                        Outline the flight and hotel requirements.
                                        """, from, to, departureDate, returnDate,
                                passengers != null ? passengers : 1, preferences))
                        .call()
                        .content();

                // Coordinate with agents asynchronously
                CompletableFuture<String> flightFuture = sendA2AMessage(
                        getFlightAgentClient(),
                        String.format("Search for flights from %s to %s, departing %s, returning %s. %d passengers. Preferences: %s",
                                from, to, departureDate, returnDate, passengers != null ? passengers : 1, preferences)
                );

                CompletableFuture<String> hotelFuture = sendA2AMessage(
                        getHotelAgentClient(),
                        String.format("Find hotels in %s for check-in %s, check-out %s. %d guests. Preferences: %s",
                                to, departureDate, returnDate, passengers != null ? passengers : 1, preferences)
                );

                // Combine results
                String flightResponse = flightFuture.join();
                String hotelResponse = hotelFuture.join();

                // Synthesize final recommendations
                String finalRecommendations = chatClient.prompt()
                        .user(String.format("""
                                Synthesize these responses into a comprehensive travel plan:
                                Coordination Plan: %s
                                Flight Response: %s
                                Hotel Response: %s
                                """, coordinationPlan, flightResponse, hotelResponse))
                        .call()
                        .content();

                return new TravelPlan(
                        tripId,
                        null, // Would parse flight response
                        null, // Would parse return flight
                        null, // Would parse hotel responses
                        "coordinated",
                        preferences,
                        finalRecommendations
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to create travel plan: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<String> processChatMessage(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Analyze intent
                String intentAnalysis = chatClient.prompt()
                        .user(String.format("""
                                Analyze this travel request:
                                User Message: %s
                                Determine if flight, hotel, or both agents should be involved.
                                """, message))
                        .call()
                        .content();

                // Determine which agents to involve
                boolean needsFlight = message.toLowerCase().contains("flight") ||
                        message.toLowerCase().contains("fly");
                boolean needsHotel = message.toLowerCase().contains("hotel") ||
                        message.toLowerCase().contains("accommodation");
                boolean needsBoth = message.toLowerCase().contains("trip") ||
                        message.toLowerCase().contains("travel") ||
                        message.toLowerCase().contains("plan");

                CompletableFuture<String> flightFuture = needsFlight || needsBoth ?
                        sendA2AMessage(getFlightAgentClient(), "Travel request: " + message) :
                        CompletableFuture.completedFuture("");

                CompletableFuture<String> hotelFuture = needsHotel || needsBoth ?
                        sendA2AMessage(getHotelAgentClient(), "Travel request: " + message) :
                        CompletableFuture.completedFuture("");

                String flightResponse = flightFuture.join();
                String hotelResponse = hotelFuture.join();

                return chatClient.prompt()
                        .user(String.format("""
                                Combine these responses:
                                Intent Analysis: %s
                                Flight Response: %s
                                Hotel Response: %s
                                """, intentAnalysis, flightResponse, hotelResponse))
                        .call()
                        .content();
            } catch (Exception e) {
                return "Error processing request: " + e.getMessage();
            }
        });
    }

    public Map<String, Object> getAgentsStatus() {
        try {
            Map<String, Object> flightAgentStatus = checkA2AAgentStatus("flight", flightAgentUrl);
            Map<String, Object> hotelAgentStatus = checkA2AAgentStatus("hotel", hotelAgentUrl);

            return Map.of(
                    "flightAgent", flightAgentStatus,
                    "hotelAgent", hotelAgentStatus,
                    "mcpServers", Map.of(
                            "flightMcp", Map.of(
                                    "url", "http://localhost:8081",
                                    "status", "operational"
                            ),
                            "hotelMcp", Map.of(
                                    "url", "http://localhost:8083",
                                    "status", "operational"
                            )
                    ),
                    "protocol", "A2A (Agent-to-Agent)",
                    "orchestrator", Map.of(
                            "version", "1.0.0",
                            "features", List.of(
                                    "A2A protocol communication",
                                    "Multi-agent coordination",
                                    "Spring AI integration",
                                    "MCP server integration"
                            )
                    ),
                    "timestamp", System.currentTimeMillis()
            );
        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to get agent status: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
        }
    }

    private Map<String, Object> checkA2AAgentStatus(String agentType, String agentUrl) {
        try {
            Client client = agentType.equals("flight") ?
                    getFlightAgentClient() : getHotelAgentClient();
            AgentCard card = client.getAgentCard();

            return Map.of(
                    "available", true,
                    "url", agentUrl,
                    "name", card.name(),
                    "description", card.description(),
                    "version", card.version(),
                    "capabilities", card.capabilities(),
                    "skillsCount", card.skills() != null ? card.skills().size() : 0,
                    "protocol", "A2A",
                    "lastCheck", System.currentTimeMillis()
            );
        } catch (Exception e) {
            return Map.of(
                    "available", false,
                    "url", agentUrl,
                    "error", e.getMessage(),
                    "protocol", "A2A",
                    "lastCheck", System.currentTimeMillis()
            );
        }
    }


    public static void main(String[] args) throws A2AClientError, A2AClientException, JsonProcessingException, InterruptedException {
        // First, get the agent card for the A2A server agent you want to connect to
        AgentCard agentCard = new A2ACardResolver(
                new JdkA2AHttpClient(),
                "http://localhost:8080/flight",
                "http://localhost:8080/flight/.well-known/agent-card.json"
        ).getAgentCard();

        System.out.println(OBJECT_MAPPER.writeValueAsString(agentCard));

        final CompletableFuture<String> messageResponse = new CompletableFuture<>();

        // Create the client using the builder
        Client client = Client
                .builder(agentCard)
                .clientConfig( // Specify general client configuration and preferences for the ClientBuilder
                        new ClientConfig.Builder()
                                .setAcceptedOutputModes(List.of("text"))
                                .setStreaming(false)
                                .build())
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                .addConsumers( //         // Create event consumers to handle responses that will be received from the A2A server
                        // (these consumers will be used for both streaming and non-streaming responses)
                        List.of(
                                (event, card) -> {
                                    if (event instanceof MessageEvent messageEvent) {
                                        // handle the messageEvent.getMessage()
                                        System.out.println("A2A message received: " + messageEvent.getMessage());
                                        Message responseMessage = messageEvent.getMessage();
                                        StringBuilder textBuilder = new StringBuilder();
                                        if (responseMessage.getParts() != null) {
                                            for (Part<?> part : responseMessage.getParts()) {
                                                if (part instanceof TextPart textPart) {
                                                    textBuilder.append(textPart.getText());
                                                }
                                            }
                                        }
                                        messageResponse.complete(textBuilder.toString());
                                    } else if (event instanceof TaskEvent taskEvent) {
                                        // handle the taskEvent.getTask()
                                        System.out.println("A2A task received: " + taskEvent.getTask());

                                    } else if (event instanceof TaskUpdateEvent updateEvent) {
                                        // handle the updateEvent.getTask()
                                        System.out.println("A2A task updated received: " + updateEvent.getUpdateEvent());
                                    } else {
                                        System.out.println("A2A unknown event received: " + event);
                                    }
                                }
                        ))
                .streamingErrorHandler(  // Create error handler for streaming errors
                        (Throwable error) -> {
                            System.err.println("Streaming error occurred: " + error.getMessage());
                            error.printStackTrace();
                            messageResponse.completeExceptionally(error);
                        })
                .build();

        // 3. Create a ClientCallContext with metadata/headers
        Map<String, Object> state = new HashMap<>();
        state.put("conversation_id", "conv_123");
        state.put("skill", "flight_search"); // Align with agent's skills

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer your_token_here"); // Replace with actual token if needed
        headers.put("X-Request-ID", "req_" + System.currentTimeMillis());

        ClientCallContext context = new ClientCallContext(state, headers);

        // 4. Create and send the message
        Message message = A2A.toUserMessage("Find flights from Yerevan to Paris on 2025-12-25");
        System.out.println("Sending message: " + OBJECT_MAPPER.writeValueAsString(message));
        client.sendMessage(message, context); // Pass the context here

        // 5. Wait for the response
        try {
            String response = messageResponse.get(10, TimeUnit.SECONDS);
            System.out.println("Response: " + response);
        } catch (Exception e) {
            System.err.println("Failed to get response: " + e.getMessage());
        }
    }
}
