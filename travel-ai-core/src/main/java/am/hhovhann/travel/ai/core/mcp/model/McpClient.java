package am.hhovhann.travel.ai.core.mcp.model;

import am.hhovhann.travel.ai.core.mcp.exception.McpException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class McpClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public McpClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<McpResponse> callTool(String serverUrl, String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                McpRequest request = new McpRequest(
                        "2.0",
                        "tools/call",
                        generateRequestId(),
                        Map.of(
                                "name", toolName,
                                "arguments", arguments
                        )
                );

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                HttpEntity<McpRequest> entity = new HttpEntity<>(request, headers);

                ResponseEntity<McpResponse> response = restTemplate.exchange(
                        serverUrl + "/mcp",
                        HttpMethod.POST,
                        entity,
                        McpResponse.class
                );

                return response.getBody();
            } catch (Exception e) {
                throw new McpException("Failed to call MCP tool: " + toolName, e);
            }
        });
    }

    public CompletableFuture<McpCapabilities> getCapabilities(String serverUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                McpRequest request = new McpRequest(
                        "2.0",
                        "initialize",
                        generateRequestId(),
                        Map.of(
                                "protocolVersion", "2024-11-05",
                                "capabilities", Map.of("tools", true)
                        )
                );

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                HttpEntity<McpRequest> entity = new HttpEntity<>(request, headers);

                ResponseEntity<McpCapabilities> response = restTemplate.exchange(
                        serverUrl + "/mcp/capabilities",
                        HttpMethod.POST,
                        entity,
                        McpCapabilities.class
                );

                return response.getBody();
            } catch (Exception e) {
                throw new McpException("Failed to get MCP capabilities", e);
            }
        });
    }

    private String generateRequestId() {
        return "req-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
}

