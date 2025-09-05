package am.hhovhann.travel.ai.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightRequest(
        @NotNull String from,
        @NotNull String to,
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull LocalDate departureDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate returnDate,
        @NotNull Integer passengers,
        String cabinClass,
        String preferences
) {
}
