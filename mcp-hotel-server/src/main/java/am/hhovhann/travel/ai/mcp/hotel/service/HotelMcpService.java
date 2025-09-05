package am.hhovhann.travel.ai.mcp.hotel.service;

import am.hhovhann.travel.ai.mcp.hotel.provider.HotelProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HotelMcpService {

    private final List<HotelProvider> hotelProviders;

    @Autowired
    public HotelMcpService(List<HotelProvider> hotelProviders) {
        this.hotelProviders = hotelProviders;
    }

    public Map<String, Object> searchHotels(Map<String, Object> arguments) {
        String destination = (String) arguments.get("destination");
        String checkIn = (String) arguments.get("checkIn");
        String checkOut = (String) arguments.get("checkOut");
        Integer guests = (Integer) arguments.get("guests");
        Integer rooms = (Integer) arguments.getOrDefault("rooms", 1);
        String preferences = (String) arguments.getOrDefault("preferences", "");

        List<Map<String, Object>> allHotels = hotelProviders.stream()
                .flatMap(provider -> provider.searchHotels(destination, checkIn, checkOut, guests, rooms, preferences).stream())
                .sorted((h1, h2) -> Double.compare(
                        ((Number) h1.get("pricePerNight")).doubleValue(),
                        ((Number) h2.get("pricePerNight")).doubleValue()
                ))
                .limit(15)
                .toList();

        return Map.of(
                "hotels", allHotels,
                "total", allHotels.size(),
                "providers", hotelProviders.stream().map(HotelProvider::getName).toList()
        );
    }

    public Map<String, Object> bookHotel(Map<String, Object> arguments) {
        String hotelId = (String) arguments.get("hotelId");
        Map<String, Object> guestDetails = (Map<String, Object>) arguments.get("guestDetails");

        for (HotelProvider provider : hotelProviders) {
            if (provider.canHandleHotel(hotelId)) {
                return provider.bookHotel(hotelId, guestDetails);
            }
        }

        throw new RuntimeException("No provider found for hotel: " + hotelId);
    }

    public Map<String, Object> getRecommendations(Map<String, Object> arguments) {
        String destination = (String) arguments.get("destination");
        String preferences = (String) arguments.get("preferences");

        List<Map<String, Object>> recommendations = hotelProviders.stream()
                .flatMap(provider -> provider.getRecommendations(destination, preferences).stream())
                .toList();

        return Map.of(
                "recommendations", recommendations,
                "total", recommendations.size()
        );
    }

    public Map<String, Object> searchNearAirport(Map<String, Object> arguments) {
        String airportCode = (String) arguments.get("airportCode");
        String checkIn = (String) arguments.get("checkIn");
        String checkOut = (String) arguments.get("checkOut");

        List<Map<String, Object>> nearbyHotels = hotelProviders.stream()
                .flatMap(provider -> provider.searchNearAirport(airportCode, checkIn, checkOut).stream())
                .toList();

        return Map.of(
                "hotels", nearbyHotels,
                "total", nearbyHotels.size(),
                "airportCode", airportCode
        );
    }

    public Map<String, Object> getHotelDetails(Map<String, Object> arguments) {
        String hotelId = (String) arguments.get("hotelId");

        for (HotelProvider provider : hotelProviders) {
            if (provider.canHandleHotel(hotelId)) {
                return provider.getHotelDetails(hotelId);
            }
        }

        return Map.of(
                "error", "Hotel not found",
                "hotelId", hotelId
        );
    }

    public List<Map<String, Object>> getAvailableTools() {
        return List.of(
                Map.of(
                        "name", "search_hotels",
                        "description", "Search for hotels based on destination, dates, and preferences",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "destination", Map.of("type", "string", "description", "Destination city or area"),
                                        "checkIn", Map.of("type", "string", "description", "Check-in date (YYYY-MM-DD)"),
                                        "checkOut", Map.of("type", "string", "description", "Check-out date (YYYY-MM-DD)"),
                                        "guests", Map.of("type", "integer", "description", "Number of guests"),
                                        "rooms", Map.of("type", "integer", "description", "Number of rooms"),
                                        "preferences", Map.of("type", "string", "description", "Additional preferences")
                                ),
                                "required", List.of("destination", "checkIn", "checkOut", "guests")
                        )
                ),
                Map.of(
                        "name", "book_hotel",
                        "description", "Book a specific hotel room",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "hotelId", Map.of("type", "string", "description", "Hotel identifier"),
                                        "guestDetails", Map.of("type", "object", "description", "Guest information")
                                ),
                                "required", List.of("hotelId", "guestDetails")
                        )
                ),
                Map.of(
                        "name", "get_recommendations",
                        "description", "Get hotel recommendations for a destination",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "destination", Map.of("type", "string", "description", "Destination city or area"),
                                        "preferences", Map.of("type", "string", "description", "User preferences")
                                ),
                                "required", List.of("destination")
                        )
                ),
                Map.of(
                        "name", "search_near_airport",
                        "description", "Find hotels near a specific airport",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "airportCode", Map.of("type", "string", "description", "Airport code (e.g., JFK, LAX)"),
                                        "checkIn", Map.of("type", "string", "description", "Check-in date (YYYY-MM-DD)"),
                                        "checkOut", Map.of("type", "string", "description", "Check-out date (YYYY-MM-DD)")
                                ),
                                "required", List.of("airportCode")
                        )
                ),
                Map.of(
                        "name", "get_hotel_details",
                        "description", "Get detailed information about a specific hotel",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "hotelId", Map.of("type", "string", "description", "Hotel identifier")
                                ),
                                "required", List.of("hotelId")
                        )
                )
        );
    }
}
