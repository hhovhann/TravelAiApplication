package am.hhovhann.travel.ai.flight.agent;

import am.hhovhann.travel.ai.core.mcp.model.McpClient;
import am.hhovhann.travel.ai.core.util.A2AMessageBuilder;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
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

import java.util.List;
import java.util.Map;

@Configuration
public class FlightAgentExecutorProducer {

    @Value("${flight.mcp.server.url:http://localhost:8081}")
    private String flightMcpServerUrl;

    // Agent Executor Bean - A2A SDK will use this to handle requests
    @Bean
    public AgentExecutor flightAgentExecutor(McpClient mcpClient) {
        return new FlightA2AExecutor(mcpClient, flightMcpServerUrl);
    }

    private static class FlightA2AExecutor implements AgentExecutor {
        private final McpClient mcpClient;
        private final String mcpServerUrl;

        public FlightA2AExecutor(McpClient mcpClient, String mcpServerUrl) {
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

                // Determine which MCP tool to call based on message content
                String mcpResult = routeToMcpServer(userMessage);

                TextPart responsePart = new TextPart(mcpResult, null);
                updater.addArtifact(List.of(responsePart), null, null, null);
                updater.complete();

            } catch (Exception e) {
                TextPart errorPart = new TextPart(
                        "Flight agent error: " + e.getMessage(), null);
                updater.addArtifact(List.of(errorPart), null, null, null);
                updater.fail(A2AMessageBuilder.createTextMessage("Flight processing failed: " + e.getMessage()));
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
                // Simple routing logic - in practice, you'd use AI to determine intent
                if (userMessage.toLowerCase().contains("search") || userMessage.toLowerCase().contains("find")) {
                    return callMcpTool("search_flights", parseFlightSearchParams(userMessage));
                } else if (userMessage.toLowerCase().contains("book")) {
                    return callMcpTool("book_flight", parseFlightBookingParams(userMessage));
                } else {
                    return "I can help you search and book flights. What would you like to do?";
                }
            } catch (Exception e) {
                return "Error processing flight request: " + e.getMessage();
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
                        .join(); // For simplicity - in production, handle async properly
            } catch (Exception e) {
                return "Failed to call MCP server: " + e.getMessage();
            }
        }

        private Map<String, Object> parseFlightSearchParams(String message) {
            // Simple parsing - in practice, use AI to extract parameters
            return Map.of(
                    "from", "NYC", // Would parse from message
                    "to", "LON",   // Would parse from message
                    "departureDate", "2024-12-15",
                    "passengers", 1
            );
        }

        private Map<String, Object> parseFlightBookingParams(String message) {
            return Map.of(
                    "flightId", "extracted-from-message",
                    "passengers", 1
            );
        }

        private String formatMcpResponse(Map<String, Object> result) {
            // Format MCP response for human consumption
            if (result.containsKey("flights")) {
                List<?> flights = (List<?>) result.get("flights");
                return String.format("Found %d flights. Here are the options: %s",
                        flights.size(), flights.toString());
            }
            return "Flight request processed: " + result.toString();
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
