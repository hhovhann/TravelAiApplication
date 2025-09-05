package am.hhovhann.travel.ai.core.mcp.model;

import java.util.Map;

public record McpTool(
        String name,
        String description,
        Map<String, Object> inputSchema
) {}
