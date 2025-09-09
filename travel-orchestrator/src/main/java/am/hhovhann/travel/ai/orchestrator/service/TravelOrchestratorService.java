package am.hhovhann.travel.ai.orchestrator.service;

import am.hhovhann.travel.ai.core.model.TravelPlan;
import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
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
import io.a2a.spec.Message;
import io.a2a.spec.SendMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class TravelOrchestratorService {

    private final ChatClient chatClient;
    private final String flightAgentUrl;
    private final String hotelAgentUrl;

    // Lazy initialization to avoid startup issues
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

    private Client getFlightAgentClient() {
        if (flightAgentClient == null) {
            try {
                // Create A2A client that will fetch agent card from the flight agent
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
                flightAgentClient = Client
                        .builder(agentCard)
                        .clientConfig(clientConfig)
                        .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                        .addConsumers(consumers)
                        .streamingErrorHandler(errorHandler)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create flight agent A2A client: " + e.getMessage(), e);
            }
        }
        return flightAgentClient;
    }

    private Client getHotelAgentClient() {
        if (hotelAgentClient == null) {
            try {
                // Create A2A client that will fetch agent card from the flight agent
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
                hotelAgentClient = Client
                        .builder(agentCard)
                        .clientConfig(clientConfig)
                        .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                        .addConsumers(consumers)
                        .streamingErrorHandler(errorHandler)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create hotel agent A2A client: " + e.getMessage(), e);
            }
        }
        return hotelAgentClient;
    }

    public CompletableFuture<TravelPlan> planTrip(Map<String, Object> travelRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String tripId = UUID.randomUUID().toString();

                // Extract travel details
                String from = (String) travelRequest.get("from");
                String to = (String) travelRequest.get("to");
                String departureDate = (String) travelRequest.get("departureDate");
                String returnDate = (String) travelRequest.get("returnDate");
                String preferences = (String) travelRequest.get("preferences");
                Integer passengers = (Integer) travelRequest.get("passengers");

                // Use Spring AI to create coordination strategy
                String coordinationPlan = chatClient.prompt()
                        .user(String.format("""
                                Create a travel coordination plan for:
                                - Trip: %s to %s
                                - Departure: %s, Return: %s
                                - Passengers: %d
                                - Preferences: %s
                                
                                Outline the flight and hotel requirements to coordinate with specialized agents.
                                """, from, to, departureDate, returnDate, passengers != null ? passengers : 1, preferences))
                        .call()
                        .content();

                // Coordinate with Flight Agent via A2A
                String flightMessage = String.format(
                        "Search for flights from %s to %s, departing %s, returning %s. %d passengers. Preferences: %s",
                        from, to, departureDate, returnDate, passengers != null ? passengers : 1, preferences
                );

                SendMessageResponse flightResponse = sendA2AMessage(getFlightAgentClient(), flightMessage);

                // Coordinate with Hotel Agent via A2A
                String hotelMessage = String.format(
                        "Find hotels in %s for check-in %s, check-out %s. %d guests. Consider proximity to airport and city center. Preferences: %s",
                        to, departureDate, returnDate, passengers != null ? passengers : 1, preferences
                );

                SendMessageResponse hotelResponse = sendA2AMessage(getHotelAgentClient(), hotelMessage);

                // Synthesize the responses using Spring AI
//                String finalRecommendations = chatClient.prompt()
//                        .user(String.format("""
//                                Synthesize these agent responses into a comprehensive travel plan:
//
//                                Coordination Plan: %s
//
//                                Flight Agent Response (ID: %s):
//                                %s
//
//                                Hotel Agent Response (ID: %s):
//                                %s
//
//                                Provide final recommendations and next steps.
//                                """, coordinationPlan,
//                                flightResponse.getId(), getResponseContent(flightResponse),
//                                hotelResponse.getId(), getResponseContent(hotelResponse)))
//                        .call()
//                        .content();

                return new TravelPlan(
                        tripId,
                        null, // Would parse flight response for structured data
                        null, // Would parse return flight
                        null, // Would parse hotel responses
                        "coordinated",
                        preferences,
                        null
                );

            } catch (Exception e) {
                throw new RuntimeException("Failed to create travel plan: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<String> processChatMessage(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Use Spring AI to analyze intent and create response strategy
                String intentAnalysis = chatClient.prompt()
                        .user(String.format("""
                                Analyze this travel request and determine the coordination strategy:
                                
                                User Message: %s
                                
                                Available A2A agents:
                                - Flight Agent: handles flight search, booking, recommendations
                                - Hotel Agent: handles hotel search, booking, recommendations
                                
                                Should I involve specific agents? Provide initial response and coordination plan.
                                """, message))
                        .call()
                        .content();

                // Determine A2A agent involvement
                if (message.toLowerCase().contains("flight") || message.toLowerCase().contains("fly")) {
                    try {
                        SendMessageResponse flightResponse = sendA2AMessage(getFlightAgentClient(), message);
                        return intentAnalysis + "\n\nFlight Agent (A2A Response ID: " + flightResponse.getId() +
                                "):\n" + getResponseContent(flightResponse);
                    } catch (Exception e) {
                        return intentAnalysis + "\n\nFlight Agent unavailable: " + e.getMessage();
                    }
                }

                if (message.toLowerCase().contains("hotel") || message.toLowerCase().contains("accommodation")) {
                    try {
                        SendMessageResponse hotelResponse = sendA2AMessage(getHotelAgentClient(), message);
                        return intentAnalysis + "\n\nHotel Agent (A2A Response ID: " + hotelResponse.getId() +
                                "):\n" + getResponseContent(hotelResponse);
                    } catch (Exception e) {
                        return intentAnalysis + "\n\nHotel Agent unavailable: " + e.getMessage();
                    }
                }

                // For general travel planning, involve both agents
                if (message.toLowerCase().contains("trip") || message.toLowerCase().contains("travel") ||
                        message.toLowerCase().contains("plan")) {
                    try {
                        SendMessageResponse flightResponse = sendA2AMessage(getFlightAgentClient(),
                                "Help with travel planning: " + message);
                        SendMessageResponse hotelResponse = sendA2AMessage(getHotelAgentClient(),
                                "Help with travel planning: " + message);

                        return intentAnalysis +
                                "\n\nFlight Agent (A2A ID: " + flightResponse.getId() + "):\n" +
                                getResponseContent(flightResponse) +
                                "\n\nHotel Agent (A2A ID: " + hotelResponse.getId() + "):\n" +
                                getResponseContent(hotelResponse);
                    } catch (Exception e) {
                        return intentAnalysis + "\n\nAgent coordination error: " + e.getMessage();
                    }
                }

                return intentAnalysis;

            } catch (Exception e) {
                return "I apologize, but I encountered an error processing your request: " + e.getMessage();
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
                            "flightMcp", Map.of("url", "http://localhost:8081", "status", "operational"),
                            "hotelMcp", Map.of("url", "http://localhost:8083", "status", "operational")
                    ),
                    "protocol", "A2A (Agent-to-Agent)",
                    "orchestrator", Map.of(
                            "version", "1.0.0",
                            "features", java.util.List.of(
                                    "A2A protocol communication",
                                    "Multi-agent coordination",
                                    "Spring AI integration",
                                    "MCP server integration"
                            )
                    )
            );
        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to get agent status: " + e.getMessage()
            );
        }
    }

    private SendMessageResponse sendA2AMessage(Client client, String messageText) {
        try {
            Message message = A2AMessageBuilder.createTextMessage(messageText);

            client.sendMessage(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send A2A message: " + e.getMessage(), e);
        }
        return null;
    }

    private String getResponseContent(SendMessageResponse response) {
        try {
            if (response.getResult() != null) {
                // Extract content based on result kind
                String resultKind = response.getResult().getKind();
                return "A2A Response (" + resultKind + "): Processing complete";
            }
            return "A2A Response ID: " + response.getId();
        } catch (Exception e) {
            return "A2A Response processing error: " + e.getMessage();
        }
    }

    private Map<String, Object> checkA2AAgentStatus(String agentType, String agentUrl) {
        try {
            Client client = "flight".equals(agentType) ? getFlightAgentClient() : getHotelAgentClient();
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
}