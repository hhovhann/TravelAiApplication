package am.hhovhann.travel.ai.mcp.hotel.provider;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class HolidayInnProvider implements HotelProvider {

    @Override
    public String getName() {
        return "Holiday Inn";
    }

    @Override
    public List<Map<String, Object>> searchHotels(String destination, String checkIn, String checkOut, Integer guests, Integer rooms, String preferences) {
        List<Map<String, Object>> hotels = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            Map<String, Object> hotel = new HashMap<>();
            hotel.put("hotelId", "HI" + i + ":" + destination.replaceAll("\\s+", ""));
            hotel.put("name", "Holiday Inn " + destination + " " + (i == 1 ? "Express" : "& Suites"));
            hotel.put("address", (200 + i * 30) + " Business Drive, " + destination);
            hotel.put("city", destination);
            hotel.put("country", "USA");
            hotel.put("starRating", 3);
            hotel.put("pricePerNight", 120.00 + (i * 40));
            hotel.put("currency", "USD");
            hotel.put("roomType", i == 1 ? "Standard Queen" : "King Suite");
            hotel.put("amenities", List.of("Free WiFi", "Free Breakfast", "Fitness Center", "Business Center"));
            hotel.put("rating", 4.0 + (i * 0.1));
            hotel.put("reviewCount", 500 + (i * 75));
            hotel.put("description", "Comfortable stay with great value in " + destination);
            hotel.put("hasWifi", true);
            hotel.put("hasParking", true);
            hotel.put("hasBreakfast", true);
            hotel.put("provider", "Holiday Inn");

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
                "bookingId", "HI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "hotelId", hotelId,
                "status", "confirmed",
                "guestDetails", guestDetails,
                "confirmationNumber", "HI" + System.currentTimeMillis() % 10000,
                "provider", "Holiday Inn",
                "includes", "Free breakfast and WiFi"
        );
    }

    @Override
    public List<Map<String, Object>> getRecommendations(String destination, String preferences) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        recommendations.add(Map.of(
                "destination", destination,
                "recommendation", "Great value Holiday Inn properties in " + destination + " with free breakfast",
                "averagePrice", 140.00,
                "popularity", "High",
                "seasonality", "Stable year-round pricing",
                "specialOffers", "Kids stay free",
                "provider", "Holiday Inn"
        ));

        return recommendations;
    }

    @Override
    public List<Map<String, Object>> searchNearAirport(String airportCode, String checkIn, String checkOut) {
        List<Map<String, Object>> hotels = new ArrayList<>();

        Map<String, Object> hotel = new HashMap<>();
        hotel.put("hotelId", "HI-AIRPORT:" + airportCode);
        hotel.put("name", "Holiday Inn Express " + airportCode + " Airport");
        hotel.put("address", "Airport Business Park");
        hotel.put("city", getAirportCity(airportCode));
        hotel.put("starRating", 3);
        hotel.put("pricePerNight", 140.00);
        hotel.put("currency", "USD");
        hotel.put("distanceFromAirport", "1.2 miles");
        hotel.put("shuttleService", true);
        hotel.put("amenities", List.of("Free Airport Shuttle", "Free WiFi", "Free Breakfast", "Fitness Center"));
        hotel.put("provider", "Holiday Inn");

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
                        "24/7 Front Desk", "Free Continental Breakfast", "Free WiFi",
                        "Fitness Center", "Business Center", "Free Parking",
                        "Indoor Pool", "Laundry Facilities"
                ),
                "checkInTime", "3:00 PM",
                "checkOutTime", "11:00 AM",
                "petPolicy", "Pets welcome with deposit",
                "cancellationPolicy", "Free cancellation until 6 PM day of arrival",
                "provider", "Holiday Inn"
        );
    }

    @Override
    public boolean canHandleHotel(String hotelId) {
        return hotelId != null && hotelId.startsWith("HI");
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

