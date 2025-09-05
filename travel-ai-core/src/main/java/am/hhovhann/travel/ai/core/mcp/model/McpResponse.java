package am.hhovhann.travel.ai.core.mcp.model;

import java.util.Map;

public record McpResponse(
        String jsonrpc,
        String id,
        Map<String, Object> result,
        McpError error
) {}
