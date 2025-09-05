package am.hhovhann.travel.ai.orchestrator.controller;

import am.hhovhann.travel.ai.core.model.TravelPlan;
import am.hhovhann.travel.ai.orchestrator.service.TravelOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/travel")
@CrossOrigin(origins = "*")
public class TravelController {

    private final TravelOrchestratorService orchestratorService;

    @Autowired
    public TravelController(TravelOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/plan")
    public CompletableFuture<ResponseEntity<TravelPlan>> planTrip(@RequestBody Map<String, Object> travelRequest) {
        return orchestratorService.planTrip(travelRequest)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/chat")
    public CompletableFuture<ResponseEntity<Map<String, String>>> chatWithTravel(@RequestBody Map<String, String> chatRequest) {
        String message = chatRequest.get("message");
        return orchestratorService.processChatMessage(message)
                .thenApply(response -> ResponseEntity.ok(Map.of("response", response)))
                .exceptionally(ex -> ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())));
    }

    @GetMapping("/agents/status")
    public ResponseEntity<Map<String, Object>> getAgentsStatus() {
        Map<String, Object> status = orchestratorService.getAgentsStatus();
        return ResponseEntity.ok(status);
    }
}
