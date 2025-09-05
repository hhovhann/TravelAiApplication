package am.hhovhann.travel.ai.core.model;

import java.time.LocalDateTime;
import java.util.Map;

public record AgentCommunication(
        String messageId,
        String fromAgent,
        String toAgent,
        String messageType,
        Map<String, Object> payload,
        LocalDateTime timestamp,
        String conversationId
) {}
