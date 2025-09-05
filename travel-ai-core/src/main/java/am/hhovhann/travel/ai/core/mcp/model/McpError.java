package am.hhovhann.travel.ai.core.mcp.model;

public record McpError(
        int code,
        String message,
        Object data
) {}
