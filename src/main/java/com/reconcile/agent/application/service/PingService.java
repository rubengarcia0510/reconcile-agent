package com.reconcile.agent.application.service;

import com.reconcile.agent.application.port.in.PingUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PingService implements PingUseCase {
    @Override
    public Mono<String> ping() {
        return Mono.just("ok");
    }
}
