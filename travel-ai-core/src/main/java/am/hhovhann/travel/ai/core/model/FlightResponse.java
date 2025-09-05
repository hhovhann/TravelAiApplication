package am.hhovhann.travel.ai.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FlightResponse(
        String flightNumber,
        String airline,
        String from,
        String to,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime departureTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime arrivalTime,
        BigDecimal price,
        String currency,
        String cabinClass,
        Integer availableSeats,
        List<String> amenities,
        String duration,
        Integer stops
) {
}
