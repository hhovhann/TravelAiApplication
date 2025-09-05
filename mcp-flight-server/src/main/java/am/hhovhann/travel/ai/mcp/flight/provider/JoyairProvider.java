package am.hhovhann.travel.ai.mcp.flight.provider;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JoyairProvider implements FlightProvider {

    @Override
    public String getName() {
        return "Joyair";
    }

    @Override
    public List<Map<String, Object>> searchFlights(String from, String to, String departureDate, String returnDate, Integer passengers, String cabinClass, String preferences) {
        // Mock implementation - in real scenario, this would call Joyair API
        List<Map<String, Object>> flights = new ArrayList<>();

        // Generate mock flight data
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> flight = new HashMap<>();
            flight.put("flightId", "JOY" + i + ":" + from + "-" + to);
            flight.put("flightNumber", "JY" + (100 + i));
            flight.put("airline", "Joyair");
            flight.put("from", from);
            flight.put("to", to);
            flight.put("departureTime", departureDate + "T" + String.format("%02d:00:00", 8 + i * 2));
            flight.put("arrivalTime", departureDate + "T" + String.format("%02d:00:00", 10 + i * 2));
            flight.put("price", 299.99 + (i * 50));
            flight.put("currency", "USD");
            flight.put("cabinClass", cabinClass);
            flight.put("availableSeats", 150 - (i * 10));
            flight.put("amenities", List.of("WiFi", "Entertainment", "Meals"));
            flight.put("duration", "2h 30m");
            flight.put("stops", 0);
            flight.put("provider", "Joyair");

            flights.add(flight);
        }
        return flights;
    }

    @Override
    public Map<String, Object> bookFlight(String flightId, Map<String, Object> passengerDetails) {
        if (!canHandleFlight(flightId)) {
            throw new RuntimeException("Cannot handle flight: " + flightId);
        }

        return Map.of(
                "bookingId", "JOY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "flightId", flightId,
                "status", "confirmed",
                "passengerDetails", passengerDetails,
                "confirmationNumber", "JOY" + System.currentTimeMillis() % 10000,
                "provider", "Joyair"
        );
    }

    @Override
    public List<Map<String, Object>> getRecommendations(String destination, String preferences) {
        // Mock recommendations based on destination
        List<Map<String, Object>> recommendations = new ArrayList<>();

        recommendations.add(Map.of(
                "destination", destination,
                "recommendation", "Best deals on " + destination + " routes with Joyair",
                "averagePrice", 350.00,
                "popularity", "High",
                "seasonality", "Peak season rates apply",
                "provider", "Joyair"
        ));

        return recommendations;
    }

    @Override
    public Map<String, Object> getFlightStatus(String flightNumber, String airline) {
        if (!canHandleAirline(airline)) {
            return Map.of("status", "unknown");
        }

        return Map.of(
                "flightNumber", flightNumber,
                "airline", airline,
                "status", "On Time",
                "departureTime", LocalDateTime.now().plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "arrivalTime", LocalDateTime.now().plusHours(4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "gate", "A12",
                "terminal", "1",
                "provider", "Joyair"
        );
    }

    @Override
    public boolean canHandleFlight(String flightId) {
        return flightId != null && flightId.startsWith("JOY");
    }

    @Override
    public boolean canHandleAirline(String airline) {
        return "Joyair".equalsIgnoreCase(airline) || "JY".equalsIgnoreCase(airline);
    }
}
