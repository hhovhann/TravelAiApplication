package am.hhovhann.travel.ai.mcp.hotel.provider;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AccorProvider implements HotelProvider {

    @Override
    public String getName() {
        return "Accor";
    }

    @Override
    public List<Map<String, Object>> searchHotels(String destination, String checkIn, String checkOut, Integer guests, Integer rooms, String preferences) {
        List<Map<String, Object>> hotels = new ArrayList<>();
        String[] brands = {"Sofitel", "Novotel", "Ibis"};
        double[] basePrices = {350.00, 160.00, 90.00};
        int[] starRatings = {5, 4, 3};

        for (int i = 0; i < brands.length; i++) {
            Map<String, Object> hotel = new HashMap<>();
            hotel.put("hotelId", "ACC" + (i + 1) + ":" + destination.replaceAll("\\s+", ""));
            hotel.put("name", brands[i] + " " + destination);
            hotel.put("address", (300 + i * 40) + " International Ave, " + destination);
            hotel.put("city", destination);
            hotel.put("country", "USA");
            hotel.put("starRating", starRatings[i]);
            hotel.put("pricePerNight", basePrices[i]);
            hotel.put("currency", "USD");
            hotel.put("roomType", i == 0 ? "Luxury Suite" : i == 1 ? "Superior Room" : "Standard Room");
            hotel.put("amenities", getAmenities(i));
            hotel.put("rating", 4.0 + (i == 0 ? 0.6 : i == 1 ? 0.3 : 0.0));
            hotel.put("reviewCount", 600 + (i * 200));
            hotel.put("description", getDescription(brands[i], destination));
            hotel.put("hasWifi", true);
            hotel.put("hasParking", i < 2);
            hotel.put("hasBreakfast", i == 0);
            hotel.put("provider", "Accor");

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
                "bookingId", "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "hotelId", hotelId,
                "status", "confirmed",
                "guestDetails", guestDetails,
                "confirmationNumber", "ACC" + System.currentTimeMillis() % 10000,
                "provider", "Accor",
                "loyaltyProgram", "ALL - Accor Live Limitless"
        );
    }

    @Override
    public List<Map<String, Object>> getRecommendations(String destination, String preferences) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        recommendations.add(Map.of(
                "destination", destination,
                "recommendation", "Diverse Accor portfolio in " + destination + " from luxury Sofitel to budget Ibis",
                "averagePrice", 200.00,
                "popularity", "High",
                "seasonality", "Variable by brand",
                "specialOffers", "ALL loyalty program benefits",
                "provider", "Accor"
        ));

        return recommendations;
    }

    @Override
    public List<Map<String, Object>> searchNearAirport(String airportCode, String checkIn, String checkOut) {
        List<Map<String, Object>> hotels = new ArrayList<>();

        Map<String, Object> hotel = new HashMap<>();
        hotel.put("hotelId", "ACC-AIRPORT:" + airportCode);
        hotel.put("name", "Novotel " + airportCode + " Airport");
        hotel.put("address", "Airport Terminal Complex");
        hotel.put("city", getAirportCity(airportCode));
        hotel.put("starRating", 4);
        hotel.put("pricePerNight", 180.00);
        hotel.put("currency", "USD");
        hotel.put("distanceFromAirport", "Connected to terminal");
        hotel.put("shuttleService", false);
        hotel.put("amenities", List.of("Direct Terminal Access", "WiFi", "Restaurant", "Fitness Center"));
        hotel.put("provider", "Accor");

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
                        "24/7 Reception", "Multilingual Staff", "Currency Exchange",
                        "Fitness Center", "Restaurant", "Bar", "Room Service",
                        "Business Facilities", "Meeting Rooms"
                ),
                "checkInTime", "3:00 PM",
                "checkOutTime", "12:00 PM",
                "petPolicy", "Pet-friendly options available",
                "cancellationPolicy", "Flexible cancellation based on rate",
                "provider", "Accor"
        );
    }

    @Override
    public boolean canHandleHotel(String hotelId) {
        return hotelId != null && hotelId.startsWith("ACC");
    }

    private List<String> getAmenities(int brandIndex) {
        return switch (brandIndex) {
            case 0 -> List.of("Luxury Spa", "Fine Dining", "Concierge", "Valet Parking", "WiFi");
            case 1 -> List.of("Restaurant", "Bar", "Fitness Center", "WiFi", "Parking");
            case 2 -> List.of("Free WiFi", "24/7 Reception", "Budget-Friendly");
            default -> List.of("WiFi");
        };
    }

    private String getDescription(String brand, String destination) {
        return switch (brand) {
            case "Sofitel" -> "Luxury French elegance in the heart of " + destination;
            case "Novotel" -> "Contemporary comfort and convenience in " + destination;
            case "Ibis" -> "Smart budget accommodation in " + destination;
            default -> "Quality accommodation in " + destination;
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
