package am.hhovhann.travel.ai.hotel.config;

import am.hhovhann.travel.ai.core.mcp.model.McpClient;
import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import io.a2a.server.PublicAgentCard;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class HotelAgentA2AConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Value("${hotel.mcp.server.url:http://localhost:8083}")
    private String hotelMcpServerUrl;

    @Bean
    @PublicAgentCard
    public AgentCard hotelAgentCard() {
        return new AgentCard.Builder()
                .name("Hotel Agent")
                .description("AI agent for hotel search and booking via MCP servers")
                .url("http://localhost:" + serverPort)
                .version("1.0.0")
                .protocolVersion("2.5")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(false)
                        .stateTransitionHistory(true)
                        .build())
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(List.of(
                        new AgentSkill.Builder()
                                .id("hotel_search")
                                .name("Hotel Search")
                                .description("Search hotels via MCP server integration")
                                .tags(List.of("hotels", "search", "mcp"))
                                .examples(List.of("Find hotels in Paris"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("hotel_booking")
                                .name("Hotel Booking")
                                .description("Book hotels via MCP server integration")
                                .tags(List.of("hotels", "booking", "mcp"))
                                .examples(List.of("Book hotel room for 3 nights"))
                                .build()
                ))
                .build();
    }

    @Bean
    public AgentExecutor hotelAgentExecutor(McpClient mcpClient) {
        return new HotelA2AExecutor(mcpClient, hotelMcpServerUrl);
    }

    private static class HotelA2AExecutor implements AgentExecutor {
        private final McpClient mcpClient;
        private final String mcpServerUrl;

        public HotelA2AExecutor(McpClient mcpClient, String mcpServerUrl) {
            this.mcpClient = mcpClient;
            this.mcpServerUrl = mcpServerUrl;
        }

        @Override
        public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            TaskUpdater updater = new TaskUpdater(context, eventQueue);

            try {
                if (context.getTask() == null) {
                    updater.submit();
                }
                updater.startWork();

                String userMessage = extractTextFromMessage(context.getMessage());
                String mcpResult = routeToMcpServer(userMessage);

                TextPart responsePart = new TextPart(mcpResult, null);
                updater.addArtifact(List.of(responsePart), null, null, null);
                updater.complete();

            } catch (Exception e) {
                TextPart errorPart = new TextPart(
                        "Hotel agent error: " + e.getMessage(), null);
                updater.addArtifact(List.of(errorPart), null, null, null);
                updater.fail(A2AMessageBuilder.createTextMessage("Hotel processing failed: " + e.getMessage()));
            }
        }

        @Override
        public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            Task task = context.getTask();
            if (task.getStatus().state() == TaskState.CANCELED) {
                throw new TaskNotCancelableError();
            }
            if (task.getStatus().state() == TaskState.COMPLETED) {
                throw new TaskNotCancelableError();
            }
            TaskUpdater updater = new TaskUpdater(context, eventQueue);
            updater.cancel();
        }

        private String routeToMcpServer(String userMessage) {
            try {
                if (userMessage.toLowerCase().contains("search") || userMessage.toLowerCase().contains("find")) {
                    return callMcpTool("search_hotels", parseHotelSearchParams(userMessage));
                } else if (userMessage.toLowerCase().contains("book")) {
                    return callMcpTool("book_hotel", parseHotelBookingParams(userMessage));
                } else {
                    return "I can help you search and book hotels. What would you like to do?";
                }
            } catch (Exception e) {
                return "Error processing hotel request: " + e.getMessage();
            }
        }

        private String callMcpTool(String toolName, Map<String, Object> arguments) {
            try {
                return mcpClient.callTool(mcpServerUrl, toolName, arguments)
                        .thenApply(response -> {
                            if (response.error() != null) {
                                return "MCP Error: " + response.error().message();
                            }
                            return formatMcpResponse(response.result());
                        })
                        .join();
            } catch (Exception e) {
                return "Failed to call MCP server: " + e.getMessage();
            }
        }

        private Map<String, Object> parseHotelSearchParams(String message) {
            return Map.of(
                    "destination", "Paris", // Would parse from message
                    "checkIn", "2024-12-15",
                    "checkOut", "2024-12-20",
                    "guests", 2
            );
        }

        private Map<String, Object> parseHotelBookingParams(String message) {
            return Map.of(
                    "hotelId", "extracted-from-message",
                    "guests", 2
            );
        }

        private String formatMcpResponse(Map<String, Object> result) {
            if (result.containsKey("hotels")) {
                List<?> hotels = (List<?>) result.get("hotels");
                return String.format("Found %d hotels. Here are the options: %s",
                        hotels.size(), hotels.toString());
            }
            return "Hotel request processed: " + result.toString();
        }

        private String extractTextFromMessage(Message message) {
            StringBuilder textBuilder = new StringBuilder();
            if (message.getParts() != null) {
                for (Part<?> part : message.getParts()) {
                    if (part instanceof TextPart textPart) {
                        textBuilder.append(textPart.getText());
                    }
                }
            }
            return textBuilder.toString();
        }
    }
}
