package am.hhovhann.travel.ai.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HotelRequest(
        @NotNull String destination,
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull LocalDate checkIn,
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull LocalDate checkOut,
        @NotNull Integer guests,
        Integer rooms,
        String preferences
) {}
