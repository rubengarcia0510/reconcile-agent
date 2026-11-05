package com.reconcile.agent.application.port.out;

import com.reconcile.agent.domain.model.Invoice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceRepository {
    Mono<Invoice> save(Invoice invoice);

    Flux<Invoice> findByRunId(String runId);
}
