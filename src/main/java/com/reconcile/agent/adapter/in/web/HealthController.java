package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.port.in.PingUseCase;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {

    private final PingUseCase pingUseCase;

    public HealthController(PingUseCase pingUseCase) {
        this.pingUseCase = pingUseCase;
    }

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return pingUseCase.ping().map(status -> Map.of("status", status));
    }
}
