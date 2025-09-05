package am.hhovhann.travel.ai.mcp.flight.service;

import am.hhovhann.travel.ai.mcp.flight.provider.FlightProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FlightMcpService {

    private final List<FlightProvider> flightProviders;

    @Autowired
    public FlightMcpService(List<FlightProvider> flightProviders) {
        this.flightProviders = flightProviders;
    }

    public Map<String, Object> searchFlights(Map<String, Object> arguments) {
        String from = (String) arguments.get("from");
        String to = (String) arguments.get("to");
        String departureDate = (String) arguments.get("departureDate");
        String returnDate = (String) arguments.get("returnDate");
        Integer passengers = (Integer) arguments.get("passengers");
        String cabinClass = (String) arguments.getOrDefault("cabinClass", "economy");
        String preferences = (String) arguments.getOrDefault("preferences", "");

        // Aggregate results from all providers
        List<Map<String, Object>> allFlights = flightProviders.stream()
                .flatMap(provider -> provider.searchFlights(from, to, departureDate, returnDate, passengers, cabinClass, preferences).stream())
                .sorted((f1, f2) -> Double.compare(
                        ((Number) f1.get("price")).doubleValue(),
                        ((Number) f2.get("price")).doubleValue()
                ))
                .limit(20)
                .toList();

        return Map.of(
                "flights", allFlights,
                "total", allFlights.size(),
                "providers", flightProviders.stream().map(FlightProvider::getName).toList()
        );
    }

    public Map<String, Object> bookFlight(Map<String, Object> arguments) {
        String flightId = (String) arguments.get("flightId");
        Map<String, Object> passengerDetails = (Map<String, Object>) arguments.get("passengerDetails");

        // Find the appropriate provider for the flight
        for (FlightProvider provider : flightProviders) {
            if (provider.canHandleFlight(flightId)) {
                return provider.bookFlight(flightId, passengerDetails);
            }
        }

        throw new RuntimeException("No provider found for flight: " + flightId);
    }

    public Map<String, Object> getRecommendations(Map<String, Object> arguments) {
        String destination = (String) arguments.get("destination");
        String preferences = (String) arguments.get("preferences");

        List<Map<String, Object>> recommendations = flightProviders.stream()
                .flatMap(provider -> provider.getRecommendations(destination, preferences).stream())
                .toList();

        return Map.of(
                "recommendations", recommendations,
                "total", recommendations.size()
        );
    }

    public Map<String, Object> getFlightStatus(Map<String, Object> arguments) {
        String flightNumber = (String) arguments.get("flightNumber");
        String airline = (String) arguments.get("airline");

        for (FlightProvider provider : flightProviders) {
            if (provider.canHandleAirline(airline)) {
                return provider.getFlightStatus(flightNumber, airline);
            }
        }

        return Map.of(
                "status", "unknown",
                "message", "Flight status not available"
        );
    }

    public List<Map<String, Object>> getAvailableTools() {
        return List.of(
                Map.of(
                        "name", "search_flights",
                        "description", "Search for flights based on origin, destination, dates, and preferences",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "from", Map.of("type", "string", "description", "Origin airport code or city"),
                                        "to", Map.of("type", "string", "description", "Destination airport code or city"),
                                        "departureDate", Map.of("type", "string", "description", "Departure date (YYYY-MM-DD)"),
                                        "returnDate", Map.of("type", "string", "description", "Return date (YYYY-MM-DD), optional"),
                                        "passengers", Map.of("type", "integer", "description", "Number of passengers"),
                                        "cabinClass", Map.of("type", "string", "description", "Cabin class (economy, business, first)"),
                                        "preferences", Map.of("type", "string", "description", "Additional preferences")
                                ),
                                "required", List.of("from", "to", "departureDate", "passengers")
                        )
                ),
                Map.of(
                        "name", "book_flight",
                        "description", "Book a specific flight",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "flightId", Map.of("type", "string", "description", "Flight identifier"),
                                        "passengerDetails", Map.of("type", "object", "description", "Passenger information")
                                ),
                                "required", List.of("flightId", "passengerDetails")
                        )
                ),
                Map.of(
                        "name", "get_recommendations",
                        "description", "Get flight recommendations for a destination",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "destination", Map.of("type", "string", "description", "Destination city or airport"),
                                        "preferences", Map.of("type", "string", "description", "User preferences")
                                ),
                                "required", List.of("destination")
                        )
                ),
                Map.of(
                        "name", "get_flight_status",
                        "description", "Get real-time flight status information",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "flightNumber", Map.of("type", "string", "description", "Flight number"),
                                        "airline", Map.of("type", "string", "description", "Airline code")
                                ),
                                "required", List.of("flightNumber", "airline")
                        )
                )
        );
    }
}
