package com.reconcile.agent.application.port.out;

import reactor.core.publisher.Mono;

public interface DocumentTextReader {
    Mono<String> read(byte[] content);
}
