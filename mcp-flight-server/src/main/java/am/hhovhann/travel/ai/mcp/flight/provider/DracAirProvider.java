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
public class DracAirProvider implements FlightProvider {

    @Override
    public String getName() {
        return "DracAir";
    }

    @Override
    public List<Map<String, Object>> searchFlights(String from, String to, String departureDate, String returnDate, Integer passengers, String cabinClass, String preferences) {
        List<Map<String, Object>> flights = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            Map<String, Object> flight = new HashMap<>();
            flight.put("flightId", "DRAC" + i + ":" + from + "-" + to);
            flight.put("flightNumber", "DR" + (300 + i));
            flight.put("airline", "DracAir");
            flight.put("from", from);
            flight.put("to", to);
            flight.put("departureTime", departureDate + "T" + String.format("%02d:15:00", 7 + i * 3));
            flight.put("arrivalTime", departureDate + "T" + String.format("%02d:30:00", 9 + i * 3));
            flight.put("price", 450.00 + (i * 75));
            flight.put("currency", "USD");
            flight.put("cabinClass", cabinClass);
            flight.put("availableSeats", 120 - (i * 20));
            flight.put("amenities", List.of("Premium WiFi", "Gourmet Meals", "Luxury Seats", "Priority Boarding"));
            flight.put("duration", "2h 15m");
            flight.put("stops", 0);
            flight.put("provider", "DracAir");

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
                "bookingId", "DRAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "flightId", flightId,
                "status", "confirmed",
                "passengerDetails", passengerDetails,
                "confirmationNumber", "DR" + System.currentTimeMillis() % 10000,
                "provider", "DracAir"
        );
    }

    @Override
    public List<Map<String, Object>> getRecommendations(String destination, String preferences) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        recommendations.add(Map.of(
                "destination", destination,
                "recommendation", "Luxury travel experience to " + destination + " with DracAir",
                "averagePrice", 500.00,
                "popularity", "Premium",
                "seasonality", "Luxury rates year-round",
                "provider", "DracAir"
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
                "status", "Delayed",
                "departureTime", LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "arrivalTime", LocalDateTime.now().plusHours(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "gate", "C15",
                "terminal", "1",
                "delay", "30 minutes",
                "reason", "Weather conditions",
                "provider", "DracAir"
        );
    }

    @Override
    public boolean canHandleFlight(String flightId) {
        return flightId != null && flightId.startsWith("DRAC");
    }

    @Override
    public boolean canHandleAirline(String airline) {
        return "DracAir".equalsIgnoreCase(airline) || "DR".equalsIgnoreCase(airline);
    }
}
