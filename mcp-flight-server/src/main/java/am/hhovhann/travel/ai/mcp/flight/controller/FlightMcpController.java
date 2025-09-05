package am.hhovhann.travel.ai.mcp.flight.controller;

import am.hhovhann.travel.ai.core.mcp.model.McpRequest;
import am.hhovhann.travel.ai.core.mcp.model.McpResponse;
import am.hhovhann.travel.ai.core.mcp.model.McpError;
import am.hhovhann.travel.ai.mcp.flight.service.FlightMcpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class FlightMcpController {

    private final FlightMcpService flightMcpService;

    @Autowired
    public FlightMcpController(FlightMcpService flightMcpService) {
        this.flightMcpService = flightMcpService;
    }

    @PostMapping
    public ResponseEntity<McpResponse> handleMcpRequest(@RequestBody McpRequest request) {
        try {
            Map<String, Object> result = switch (request.method()) {
                case "tools/call" -> handleToolCall(request);
                case "initialize" -> handleInitialize(request);
                case "tools/list" -> handleToolsList(request);
                default -> throw new IllegalArgumentException("Unknown method: " + request.method());
            };

            McpResponse response = new McpResponse(
                    "2.0",
                    request.id(),
                    result,
                    null
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            McpError error = new McpError(-32603, e.getMessage(), null);
            McpResponse errorResponse = new McpResponse("2.0", request.id(), null, error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolCall(McpRequest request) {
        Map<String, Object> params = request.params();
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        return switch (toolName) {
            case "search_flights" -> flightMcpService.searchFlights(arguments);
            case "book_flight" -> flightMcpService.bookFlight(arguments);
            case "get_recommendations" -> flightMcpService.getRecommendations(arguments);
            case "get_flight_status" -> flightMcpService.getFlightStatus(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }

    private Map<String, Object> handleInitialize(McpRequest request) {
        return Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                        "tools", true,
                        "resources", false,
                        "prompts", false
                ),
                "serverInfo", Map.of(
                        "name", "Flight MCP Server",
                        "version", "1.0.0"
                )
        );
    }

    private Map<String, Object> handleToolsList(McpRequest request) {
        return Map.of(
                "tools", flightMcpService.getAvailableTools()
        );
    }

    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        return ResponseEntity.ok(Map.of(
                "tools", flightMcpService.getAvailableTools(),
                "capabilities", Map.of(
                        "tools", true,
                        "resources", false,
                        "prompts", false
                )
        ));
    }
}
