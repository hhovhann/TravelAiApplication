package am.hhovhann.travel.ai.hotel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.a2a.server.ServerCallContext;
import io.a2a.server.auth.UnauthenticatedUser;
import io.a2a.server.auth.User;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.DeleteTaskPushNotificationConfigRequest;
import io.a2a.spec.GetAuthenticatedExtendedCardRequest;
import io.a2a.spec.GetTaskPushNotificationConfigRequest;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.JSONRPCErrorResponse;
import io.a2a.spec.JSONRPCRequest;
import io.a2a.spec.JSONRPCResponse;
import io.a2a.spec.ListTaskPushNotificationConfigRequest;
import io.a2a.spec.NonStreamingJSONRPCRequest;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SetTaskPushNotificationConfigRequest;
import io.a2a.spec.UnsupportedOperationError;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
public class HotelA2AController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotelA2AController.class);

    private final AgentCard hotelAgentCard;
    private final JSONRPCHandler jsonRpcHandler;
    private final ObjectMapper objectMapper;

    public HotelA2AController(
            @Qualifier("hotelAgentCard") AgentCard hotelAgentCard,
            @Qualifier("hotelJSONRPCHandler") JSONRPCHandler jsonRpcHandler,
            ObjectMapper objectMapper) {
        this.hotelAgentCard = hotelAgentCard;
        this.jsonRpcHandler = jsonRpcHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * Get hotel agent card information
     */
    @GetMapping("/.well-known/agent-card.json")
    public ResponseEntity<AgentCard> getAgentCard() {
        LOGGER.debug("Serving hotel agent card");
        return ResponseEntity.ok(hotelAgentCard);
    }

    /**
     * Handles incoming POST requests to the main A2A endpoint. Dispatches the
     * request to the appropriate JSON-RPC handler method and returns the response.
     *
     * @param requestBody the raw JSON-RPC request body
     * @param httpRequest the HTTP servlet request
     * @return the JSON-RPC response which may be an error response
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json-rpc"}, produces = {MediaType.APPLICATION_JSON_VALUE, "application/json-rpc"})
    public ResponseEntity<JSONRPCResponse<?>> handleNonStreamingRequests(
            @RequestBody String requestBody,
            HttpServletRequest httpRequest) {

        ServerCallContext context = createCallContext(httpRequest);
        LOGGER.debug("Handling hotel non-streaming request");
        LOGGER.debug("Request body: {}", requestBody);

        try {
            // First, just read "method" and "id" without binding to abstract type
            JsonNode root = objectMapper.readTree(requestBody);
            String method = root.path("method").asText();
            String id = root.path("id").asText();

            LOGGER.debug("Parsed generic request: method={}, id={}", method, id);

            // Now parse into specific request types based on method
            NonStreamingJSONRPCRequest<?> request = parseSpecificRequest(requestBody, method);

            JSONRPCResponse<?> response = processNonStreamingRequest(request, context);
            LOGGER.debug("Sending response: {}", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.error("Error processing hotel request", e);
            JSONRPCError error = new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
            JSONRPCResponse<?> errorResponse = new JSONRPCErrorResponse("unknown", error);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private NonStreamingJSONRPCRequest<?> parseSpecificRequest(String requestBody, String method) throws Exception {
        return switch (method) {
            case "message/send" -> objectMapper.readValue(requestBody, SendMessageRequest.class);
            case "tasks/get" -> objectMapper.readValue(requestBody, GetTaskRequest.class);
            case "tasks/cancel" -> objectMapper.readValue(requestBody, CancelTaskRequest.class);
            case "tasks/pushNotificationConfig/set" ->
                    objectMapper.readValue(requestBody, SetTaskPushNotificationConfigRequest.class);
            case "tasks/pushNotificationConfig/get" ->
                    objectMapper.readValue(requestBody, GetTaskPushNotificationConfigRequest.class);
            case "tasks/pushNotificationConfig/list" ->
                    objectMapper.readValue(requestBody, ListTaskPushNotificationConfigRequest.class);
            case "tasks/pushNotificationConfig/delete" ->
                    objectMapper.readValue(requestBody, DeleteTaskPushNotificationConfigRequest.class);
            case "agent/authenticatedExtendedCard" ->
                    objectMapper.readValue(requestBody, GetAuthenticatedExtendedCardRequest.class);
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };
    }

    private ServerCallContext createCallContext(HttpServletRequest request) {
        // For now, use unauthenticated user - you can add authentication later
        User user = UnauthenticatedUser.INSTANCE;

        Map<String, Object> state = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }

        state.put("headers", headers);
        return new ServerCallContext(user, state);
    }

    private JSONRPCResponse<?> processNonStreamingRequest(NonStreamingJSONRPCRequest<?> request,
                                                          ServerCallContext context) {
        LOGGER.debug("Processing request type: {}", request.getClass().getSimpleName());

        try {
            return switch (request) {
                case SendMessageRequest req -> {
                    LOGGER.debug("Handling SendMessageRequest");
                    yield jsonRpcHandler.onMessageSend(req, context);
                }
                case GetTaskRequest req -> {
                    LOGGER.debug("Handling GetTaskRequest");
                    yield jsonRpcHandler.onGetTask(req, context);
                }
                case CancelTaskRequest req -> {
                    LOGGER.debug("Handling CancelTaskRequest");
                    yield jsonRpcHandler.onCancelTask(req, context);
                }
                case SetTaskPushNotificationConfigRequest req -> {
                    LOGGER.debug("Handling SetTaskPushNotificationConfigRequest");
                    yield jsonRpcHandler.setPushNotificationConfig(req, context);
                }
                case GetTaskPushNotificationConfigRequest req -> {
                    LOGGER.debug("Handling GetTaskPushNotificationConfigRequest");
                    yield jsonRpcHandler.getPushNotificationConfig(req, context);
                }
                case ListTaskPushNotificationConfigRequest req -> {
                    LOGGER.debug("Handling ListTaskPushNotificationConfigRequest");
                    yield jsonRpcHandler.listPushNotificationConfig(req, context);
                }
                case DeleteTaskPushNotificationConfigRequest req -> {
                    LOGGER.debug("Handling DeleteTaskPushNotificationConfigRequest");
                    yield jsonRpcHandler.deletePushNotificationConfig(req, context);
                }
                case GetAuthenticatedExtendedCardRequest req -> {
                    LOGGER.debug("Handling GetAuthenticatedExtendedCardRequest");
                    yield jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(req, context);
                }
                default -> {
                    LOGGER.warn("Unsupported request type: {}", request.getClass());
                    yield generateErrorResponse(request, new UnsupportedOperationError());
                }
            };
        } catch (Exception e) {
            LOGGER.error("Error in processNonStreamingRequest", e);
            return generateErrorResponse(request, new JSONRPCError(-32603, "Processing error: " + e.getMessage(), null));
        }
    }

    private JSONRPCResponse<?> generateErrorResponse(JSONRPCRequest<?> request, JSONRPCError error) {
        return new JSONRPCErrorResponse(request != null ? request.getId() : "unknown", error);
    }
}