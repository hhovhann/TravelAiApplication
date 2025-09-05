package am.hhovhann.travel.ai.mcp.flight.provider;

import java.util.List;
import java.util.Map;

public interface FlightProvider {
    String getName();
    List<Map<String, Object>> searchFlights(String from, String to, String departureDate, String returnDate, Integer passengers, String cabinClass, String preferences);
    Map<String, Object> bookFlight(String flightId, Map<String, Object> passengerDetails);
    List<Map<String, Object>> getRecommendations(String destination, String preferences);
    Map<String, Object> getFlightStatus(String flightNumber, String airline);
    boolean canHandleFlight(String flightId);
    boolean canHandleAirline(String airline);
}
