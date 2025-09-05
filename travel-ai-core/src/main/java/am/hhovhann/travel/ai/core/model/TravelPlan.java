package am.hhovhann.travel.ai.core.model;

import java.util.List;

public record TravelPlan(
        String tripId,
        FlightResponse outboundFlight,
        FlightResponse returnFlight,
        List<HotelResponse> hotels,
        String status,
        String userPreferences,
        String agentRecommendations
) {}
