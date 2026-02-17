package org.minjulog.feedserver.presentation.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HealthRestController {

    @Value("${env.DEPLOY_COLOR}")
    private String deployColor;

    @GetMapping({"/health", "/api/health"})
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", Instant.now().toString()));
    }

    @GetMapping("/api/deploy-color")
    public ResponseEntity<DeployColorResponse> deployColor() {
        return ResponseEntity.ok(new DeployColorResponse(deployColor));
    }

    public record HealthResponse(
            String status,
            String timestamp
    ) {
    }

    public record DeployColorResponse(
            String deployColor
    ) {
    }
}
