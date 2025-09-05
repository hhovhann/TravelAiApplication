package am.hhovhann.travel.ai.core.mcp.model;

import java.util.List;
import java.util.Map;

public record McpCapabilities(
        String protocolVersion,
        Map<String, Object> capabilities,
        List<McpTool> tools
) {}
