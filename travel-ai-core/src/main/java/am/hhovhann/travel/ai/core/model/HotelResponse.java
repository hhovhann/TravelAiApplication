package am.hhovhann.travel.ai.core.model;

import java.math.BigDecimal;
import java.util.List;

public record HotelResponse(
        String hotelId,
        String name,
        String address,
        String city,
        String country,
        Integer starRating,
        BigDecimal pricePerNight,
        String currency,
        String roomType,
        List<String> amenities,
        Double rating,
        Integer reviewCount,
        String description,
        boolean hasWifi,
        boolean hasParking,
        boolean hasBreakfast
) {}
