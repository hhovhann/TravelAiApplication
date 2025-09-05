package am.hhovhann.travel.ai.mcp.hotel.provider;

import java.util.List;
import java.util.Map;

public interface HotelProvider {
    String getName();
    List<Map<String, Object>> searchHotels(String destination, String checkIn, String checkOut, Integer guests, Integer rooms, String preferences);
    Map<String, Object> bookHotel(String hotelId, Map<String, Object> guestDetails);
    List<Map<String, Object>> getRecommendations(String destination, String preferences);
    List<Map<String, Object>> searchNearAirport(String airportCode, String checkIn, String checkOut);
    Map<String, Object> getHotelDetails(String hotelId);
    boolean canHandleHotel(String hotelId);
}

