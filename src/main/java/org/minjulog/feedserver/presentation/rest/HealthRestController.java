package org.minjulog.feedserver.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HealthRestController {

    @GetMapping({"/health", "/api/health"})
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", Instant.now().toString()));
    }

    public record HealthResponse(
            String status,
            String timestamp
    ) {
    }
}
