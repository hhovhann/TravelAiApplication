package am.hhovhann.travel.ai.core.mcp.model;

import java.util.Map;

public record McpRequest(
        String jsonrpc,
        String method,
        String id,
        Map<String, Object> params
) {}
