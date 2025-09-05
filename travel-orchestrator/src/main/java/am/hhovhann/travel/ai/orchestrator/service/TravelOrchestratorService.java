package am.hhovhann.travel.ai.orchestrator.service;

import am.hhovhann.travel.ai.core.model.*;
import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import io.a2a.client.A2AClient;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TravelOrchestratorService {

    private final ChatClient chatClient;
    private final A2AClient flightAgentClient;
    private final A2AClient hotelAgentClient;

    public TravelOrchestratorService(
            ChatClient.Builder chatClientBuilder,
            @Value("${agents.flight.url:http://localhost:8080}") String flightAgentUrl,
            @Value("${agents.hotel.url:http://localhost:8082}") String hotelAgentUrl) {

        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are a Travel Orchestrator that coordinates between Flight and Hotel agents to create comprehensive travel plans.
                        
                        Your responsibilities:
                        1. Understand user travel requirements comprehensively
                        2. Coordinate with Flight Agent for flight arrangements
                        3. Coordinate with Hotel Agent for accommodation arrangements
                        4. Create cohesive travel plans that optimize the entire journey
                        5. Handle complex multi-agent conversations and dependencies
                        
                        You can communicate with specialized agents to get the best options for users.
                        Always provide clear, comprehensive travel recommendations.
                        """)
                .build();

        this.flightAgentClient = new A2AClient(flightAgentUrl);
        this.hotelAgentClient = new A2AClient(hotelAgentUrl);
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

                // Coordinate with Flight Agent
                String flightMessage = String.format(
                        "Search for flights from %s to %s, departing %s, returning %s. Preferences: %s",
                        from, to, departureDate, returnDate, preferences
                );

                SendMessageResponse flightResponse = communicateWithAgent(flightAgentClient, flightMessage);

                // Coordinate with Hotel Agent based on flight destination
                String hotelMessage = String.format(
                        "Find hotels in %s for check-in %s, check-out %s. Consider proximity to airport and city center. Preferences: %s",
                        to, departureDate, returnDate, preferences
                );

                SendMessageResponse hotelResponse = communicateWithAgent(hotelAgentClient, hotelMessage);

                // Create travel plan (simplified - in real implementation, you'd parse agent responses)
                return new TravelPlan(
                        tripId,
                        null, // Would parse flight response
                        null, // Would parse return flight
                        null, // Would parse hotel responses
                        "planned",
                        preferences,
                        "Coordinated plan with flight and hotel agents. Task IDs: Flight=" +
                                flightResponse.getId() + ", Hotel=" + hotelResponse.getId()
                );

            } catch (Exception e) {
                throw new RuntimeException("Failed to create travel plan: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<String> processChatMessage(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Use AI to understand the intent and decide which agents to involve
                String aiResponse = chatClient.prompt()
                        .user(message)
                        .call()
                        .content();

                // Determine if we need to involve specific agents
                if (message.toLowerCase().contains("flight") || message.toLowerCase().contains("fly")) {
                    SendMessageResponse flightResponse = communicateWithAgent(flightAgentClient, message);
                    return aiResponse + "\n\nI've coordinated with the Flight Agent (Task: " +
                            flightResponse.getId() + ") for detailed flight assistance.";
                }

                if (message.toLowerCase().contains("hotel") || message.toLowerCase().contains("accommodation")) {
                    SendMessageResponse hotelResponse = communicateWithAgent(hotelAgentClient, message);
                    return aiResponse + "\n\nI've coordinated with the Hotel Agent (Task: " +
                            hotelResponse.getId() + ") for detailed hotel assistance.";
                }

                return aiResponse;

            } catch (org.springframework.ai.retry.NonTransientAiException e) {
                return "I apologize, but there was an authentication or API issue: " + e.getMessage() + 
                       ". Please check your OpenAI API key configuration.";
            } catch (org.springframework.web.client.ResourceAccessException e) {
                return "I apologize, but I couldn't connect to the AI service. Please check your network connection.";
            } catch (Exception e) {
                return "I apologize, but I encountered an error processing your request: " + e.getMessage() + 
                       ". Error type: " + e.getClass().getSimpleName();
            }
        });
    }

    public Map<String, Object> getAgentsStatus() {
        try {
            // Check agent availability
            boolean flightAgentAvailable = checkAgentAvailability(flightAgentClient);
            boolean hotelAgentAvailable = checkAgentAvailability(hotelAgentClient);

            return Map.of(
                    "flightAgent", Map.of(
                            "available", flightAgentAvailable,
                            "url", "http://localhost:8080"
                    ),
                    "hotelAgent", Map.of(
                            "available", hotelAgentAvailable,
                            "url", "http://localhost:8082"
                    ),
                    "mcpServers", Map.of(
                            "flightMcp", Map.of("url", "http://localhost:8081", "status", "unknown"),
                            "hotelMcp", Map.of("url", "http://localhost:8083", "status", "unknown")
                    )
            );
        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to get agent status: " + e.getMessage()
            );
        }
    }

    private SendMessageResponse communicateWithAgent(A2AClient agentClient, String message) {
        try {
            Message agentMessage = A2AMessageBuilder.createTextMessage(message);

            MessageSendParams params = new MessageSendParams.Builder()
                    .message(agentMessage)
                    .build();

            return agentClient.sendMessage(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with agent: " + e.getMessage(), e);
        }
    }

    private boolean checkAgentAvailability(A2AClient agentClient) {
        try {
            agentClient.getAgentCard();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
