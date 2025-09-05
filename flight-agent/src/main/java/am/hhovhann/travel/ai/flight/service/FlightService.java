package am.hhovhann.travel.ai.flight.service;

import am.hhovhann.travel.ai.core.model.FlightRequest;
import am.hhovhann.travel.ai.core.model.FlightResponse;
import am.hhovhann.travel.ai.core.mcp.model.McpClient;
import am.hhovhann.travel.ai.core.mcp.model.McpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FlightService {
    private final McpClient mcpClient;
    private final String flightMcpServerUrl;

    public FlightService(McpClient mcpClient,
                         @Value("${flight.mcp.server.url:http://localhost:8081}") String flightMcpServerUrl) {
        this.mcpClient = mcpClient;
        this.flightMcpServerUrl = flightMcpServerUrl;
    }

    public CompletableFuture<List<FlightResponse>> searchFlights(FlightRequest request) {
        return mcpClient.callTool(
                flightMcpServerUrl,
                "search_flights",
                Map.of(
                        "from", request.from(),
                        "to", request.to(),
                        "departureDate", request.departureDate().toString(),
                        "returnDate", request.returnDate() != null ? request.returnDate().toString() : null,
                        "passengers", request.passengers(),
                        "cabinClass", request.cabinClass() != null ? request.cabinClass() : "economy",
                        "preferences", request.preferences()
                )
        ).thenApply(this::parseFlightResults);
    }

    public CompletableFuture<FlightResponse> bookFlight(String flightId, Map<String, Object> passengerDetails) {
        return mcpClient.callTool(
                flightMcpServerUrl,
                "book_flight",
                Map.of(
                        "flightId", flightId,
                        "passengerDetails", passengerDetails
                )
        ).thenApply(this::parseFlightBooking);
    }

    public CompletableFuture<List<FlightResponse>> getFlightRecommendations(String destination, String preferences) {
        return mcpClient.callTool(
                flightMcpServerUrl,
                "get_recommendations",
                Map.of(
                        "destination", destination,
                        "preferences", preferences
                )
        ).thenApply(this::parseFlightResults);
    }

    @SuppressWarnings("unchecked")
    private List<FlightResponse> parseFlightResults(McpResponse response) {
        if (response.error() != null) {
            throw new RuntimeException("Flight search failed: " + response.error().message());
        }

        List<Map<String, Object>> flights = (List<Map<String, Object>>) response.result().get("flights");
        return flights.stream()
                .map(this::mapToFlightResponse)
                .toList();
    }

    private FlightResponse parseFlightBooking(McpResponse response) {
        if (response.error() != null) {
            throw new RuntimeException("Flight booking failed: " + response.error().message());
        }

        Map<String, Object> booking = (Map<String, Object>) response.result().get("booking");
        return mapToFlightResponse(booking);
    }

    private FlightResponse mapToFlightResponse(Map<String, Object> flightData) {
        // Implementation to map from MCP response to FlightResponse
        // This would typically include parsing the flight data structure
        return new FlightResponse(
                (String) flightData.get("flightNumber"),
                (String) flightData.get("airline"),
                (String) flightData.get("from"),
                (String) flightData.get("to"),
                null, // Parse departureTime from flightData
                null, // Parse arrivalTime from flightData
                null, // Parse price from flightData
                (String) flightData.get("currency"),
                (String) flightData.get("cabinClass"),
                (Integer) flightData.get("availableSeats"),
                (List<String>) flightData.get("amenities"),
                (String) flightData.get("duration"),
                (Integer) flightData.get("stops")
        );
    }
}
