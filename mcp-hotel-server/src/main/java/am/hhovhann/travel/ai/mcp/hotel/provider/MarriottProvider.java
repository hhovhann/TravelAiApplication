package am.hhovhann.travel.ai.mcp.hotel.provider;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MarriottProvider implements HotelProvider {

    @Override
    public String getName() {
        return "Marriott";
    }

    @Override
    public List<Map<String, Object>> searchHotels(String destination, String checkIn, String checkOut, Integer guests, Integer rooms, String preferences) {
        List<Map<String, Object>> hotels = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> hotel = new HashMap<>();
            hotel.put("hotelId", "MAR" + i + ":" + destination.replaceAll("\\s+", ""));
            hotel.put("name", "Marriott " + destination + " " + getHotelType(i));
            hotel.put("address", (100 + i * 50) + " Main Street, " + destination);
            hotel.put("city", destination);
            hotel.put("country", "USA");
            hotel.put("starRating", 4 + (i > 2 ? 1 : 0));
            hotel.put("pricePerNight", 180.00 + (i * 60));
            hotel.put("currency", "USD");
            hotel.put("roomType", getRoomType(i));
            hotel.put("amenities", List.of("WiFi", "Fitness Center", "Business Center", "Room Service", "Concierge"));
            hotel.put("rating", 4.2 + (i * 0.2));
            hotel.put("reviewCount", 850 + (i * 100));
            hotel.put("description", "Luxury accommodation in the heart of " + destination);
            hotel.put("hasWifi", true);
            hotel.put("hasParking", true);
            hotel.put("hasBreakfast", i > 1);
            hotel.put("provider", "Marriott");

            hotels.add(hotel);
        }
        return hotels;
    }


    @Override
    public Map<String, Object> bookHotel(String hotelId, Map<String, Object> guestDetails) {
        if (!canHandleHotel(hotelId)) {
            throw new RuntimeException("Cannot handle hotel: " + hotelId);
        }

        return Map.of(
                "bookingId", "MAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "hotelId", hotelId,
                "status", "confirmed",
                "guestDetails", guestDetails,
                "confirmationNumber", "MAR" + System.currentTimeMillis() % 10000,
                "provider", "Marriott",
                "loyaltyPoints", 500
        );
    }

    @Override
    public List<Map<String, Object>> getRecommendations(String destination, String preferences) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        recommendations.add(Map.of(
                "destination", destination,
                "recommendation", "Premium Marriott properties in " + destination + " with excellent business facilities",
                "averagePrice", 280.00,
                "popularity", "Very High",
                "seasonality", "Peak season premium applies",
                "specialOffers", "Marriott Bonvoy members get 10% off",
                "provider", "Marriott"
        ));

        return recommendations;
    }

    @Override
    public List<Map<String, Object>> searchNearAirport(String airportCode, String checkIn, String checkOut) {
        List<Map<String, Object>> hotels = new ArrayList<>();

        Map<String, Object> hotel = new HashMap<>();
        hotel.put("hotelId", "MAR-AIRPORT:" + airportCode);
        hotel.put("name", "Marriott " + airportCode + " Airport");
        hotel.put("address", "Airport Terminal Area");
        hotel.put("city", getAirportCity(airportCode));
        hotel.put("starRating", 4);
        hotel.put("pricePerNight", 220.00);
        hotel.put("currency", "USD");
        hotel.put("distanceFromAirport", "0.5 miles");
        hotel.put("shuttleService", true);
        hotel.put("amenities", List.of("Airport Shuttle", "WiFi", "Fitness Center", "Restaurant"));
        hotel.put("provider", "Marriott");

        hotels.add(hotel);
        return hotels;
    }


    @Override
    public Map<String, Object> getHotelDetails(String hotelId) {
        if (!canHandleHotel(hotelId)) {
            return Map.of("error", "Hotel not found");
        }

        return Map.of(
                "hotelId", hotelId,
                "detailedAmenities", List.of(
                        "24/7 Front Desk", "Concierge Service", "Valet Parking",
                        "Fitness Center", "Swimming Pool", "Business Center",
                        "Restaurant", "Room Service", "Laundry Service"
                ),
                "checkInTime", "3:00 PM",
                "checkOutTime", "12:00 PM",
                "petPolicy", "Pets allowed with fee",
                "cancellationPolicy", "Free cancellation until 6 PM day before arrival",
                "provider", "Marriott"
        );
    }

    @Override
    public boolean canHandleHotel(String hotelId) {
        return hotelId != null && hotelId.startsWith("MAR");
    }

    private String getHotelType(int index) {
        return switch (index) {
            case 1 -> "Downtown";
            case 2 -> "Suites";
            case 3 -> "Executive";
            default -> "Hotel";
        };
    }

    private String getRoomType(int index) {
        return switch (index) {
            case 1 -> "Standard King";
            case 2 -> "Executive Suite";
            case 3 -> "Presidential Suite";
            default -> "Standard Room";
        };
    }

    private String getAirportCity(String airportCode) {
        return switch (airportCode.toUpperCase()) {
            case "JFK", "LGA", "EWR" -> "New York";
            case "LAX" -> "Los Angeles";
            case "ORD" -> "Chicago";
            case "DFW" -> "Dallas";
            case "ATL" -> "Atlanta";
            default -> "Airport City";
        };
    }
}
