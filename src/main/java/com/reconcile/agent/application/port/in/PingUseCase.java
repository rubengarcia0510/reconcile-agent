package com.reconcile.agent.application.port.in;

import reactor.core.publisher.Mono;

public interface PingUseCase {
    Mono<String> ping();
}
