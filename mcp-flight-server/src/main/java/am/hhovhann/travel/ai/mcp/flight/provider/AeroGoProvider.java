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
public class AeroGoProvider implements FlightProvider {

    @Override
    public String getName() {
        return "AeroGo";
    }

    @Override
    public List<Map<String, Object>> searchFlights(String from, String to, String departureDate, String returnDate, Integer passengers, String cabinClass, String preferences) {
        List<Map<String, Object>> flights = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            Map<String, Object> flight = new HashMap<>();
            flight.put("flightId", "AERO" + i + ":" + from + "-" + to);
            flight.put("flightNumber", "AG" + (200 + i));
            flight.put("airline", "AeroGo");
            flight.put("from", from);
            flight.put("to", to);
            flight.put("departureTime", departureDate + "T" + String.format("%02d:30:00", 9 + i * 2));
            flight.put("arrivalTime", departureDate + "T" + String.format("%02d:45:00", 11 + i * 2));
            flight.put("price", 275.50 + (i * 45));
            flight.put("currency", "USD");
            flight.put("cabinClass", cabinClass);
            flight.put("availableSeats", 180 - (i * 15));
            flight.put("amenities", List.of("WiFi", "Power Outlets", "Snacks"));
            flight.put("duration", "2h 15m");
            flight.put("stops", i > 2 ? 1 : 0);
            flight.put("provider", "AeroGo");

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
                "bookingId", "AERO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "flightId", flightId,
                "status", "confirmed",
                "passengerDetails", passengerDetails,
                "confirmationNumber", "AG" + System.currentTimeMillis() % 10000,
                "provider", "AeroGo"
        );
    }

    @Override
    public List<Map<String, Object>> getRecommendations(String destination, String preferences) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        recommendations.add(Map.of(
                "destination", destination,
                "recommendation", "Premium service to " + destination + " with AeroGo",
                "averagePrice", 320.00,
                "popularity", "Medium",
                "seasonality", "Standard rates",
                "provider", "AeroGo"
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
                "status", "Boarding",
                "departureTime", LocalDateTime.now().plusMinutes(45).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "arrivalTime", LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "gate", "B8",
                "terminal", "2",
                "provider", "AeroGo"
        );
    }

    @Override
    public boolean canHandleFlight(String flightId) {
        return flightId != null && flightId.startsWith("AERO");
    }

    @Override
    public boolean canHandleAirline(String airline) {
        return "AeroGo".equalsIgnoreCase(airline) || "AG".equalsIgnoreCase(airline);
    }
}
