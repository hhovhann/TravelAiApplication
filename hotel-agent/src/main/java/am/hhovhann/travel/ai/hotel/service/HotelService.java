package am.hhovhann.travel.ai.hotel.service;

import am.hhovhann.travel.ai.core.model.HotelRequest;
import am.hhovhann.travel.ai.core.model.HotelResponse;
import am.hhovhann.travel.ai.core.mcp.model.McpClient;
import am.hhovhann.travel.ai.core.mcp.model.McpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class HotelService {
    private final McpClient mcpClient;
    private final String hotelMcpServerUrl;

    public HotelService(McpClient mcpClient,
                        @Value("${hotel.mcp.server.url:http://localhost:8083}") String hotelMcpServerUrl) {
        this.mcpClient = mcpClient;
        this.hotelMcpServerUrl = hotelMcpServerUrl;
    }

    public CompletableFuture<List<HotelResponse>> searchHotels(HotelRequest request) {
        return mcpClient.callTool(
                hotelMcpServerUrl,
                "search_hotels",
                Map.of(
                        "destination", request.destination(),
                        "checkIn", request.checkIn().toString(),
                        "checkOut", request.checkOut().toString(),
                        "guests", request.guests(),
                        "rooms", request.rooms() != null ? request.rooms() : 1,
                        "preferences", request.preferences() != null ? request.preferences() : ""
                )
        ).thenApply(this::parseHotelResults);
    }

    public CompletableFuture<HotelResponse> bookHotel(String hotelId, Map<String, Object> guestDetails) {
        return mcpClient.callTool(
                hotelMcpServerUrl,
                "book_hotel",
                Map.of(
                        "hotelId", hotelId,
                        "guestDetails", guestDetails
                )
        ).thenApply(this::parseHotelBooking);
    }

    public CompletableFuture<List<HotelResponse>> getHotelRecommendations(String destination, String preferences) {
        return mcpClient.callTool(
                hotelMcpServerUrl,
                "get_recommendations",
                Map.of(
                        "destination", destination,
                        "preferences", preferences
                )
        ).thenApply(this::parseHotelResults);
    }

    public CompletableFuture<List<HotelResponse>> findHotelsNearAirport(String airportCode, String checkIn, String checkOut) {
        return mcpClient.callTool(
                hotelMcpServerUrl,
                "search_near_airport",
                Map.of(
                        "airportCode", airportCode,
                        "checkIn", checkIn,
                        "checkOut", checkOut
                )
        ).thenApply(this::parseHotelResults);
    }

    @SuppressWarnings("unchecked")
    private List<HotelResponse> parseHotelResults(McpResponse response) {
        if (response.error() != null) {
            throw new RuntimeException("Hotel search failed: " + response.error().message());
        }

        List<Map<String, Object>> hotels = (List<Map<String, Object>>) response.result().get("hotels");
        return hotels.stream()
                .map(this::mapToHotelResponse)
                .toList();
    }

    private HotelResponse parseHotelBooking(McpResponse response) {
        if (response.error() != null) {
            throw new RuntimeException("Hotel booking failed: " + response.error().message());
        }

        Map<String, Object> booking = (Map<String, Object>) response.result().get("booking");
        return mapToHotelResponse(booking);
    }

    private HotelResponse mapToHotelResponse(Map<String, Object> hotelData) {
        return new HotelResponse(
                (String) hotelData.get("hotelId"),
                (String) hotelData.get("name"),
                (String) hotelData.get("address"),
                (String) hotelData.get("city"),
                (String) hotelData.get("country"),
                (Integer) hotelData.get("starRating"),
                new BigDecimal(hotelData.get("pricePerNight").toString()),
                (String) hotelData.get("currency"),
                (String) hotelData.get("roomType"),
                (List<String>) hotelData.get("amenities"),
                (Double) hotelData.get("rating"),
                (Integer) hotelData.get("reviewCount"),
                (String) hotelData.get("description"),
                (Boolean) hotelData.getOrDefault("hasWifi", false),
                (Boolean) hotelData.getOrDefault("hasParking", false),
                (Boolean) hotelData.getOrDefault("hasBreakfast", false)
        );
    }
}
