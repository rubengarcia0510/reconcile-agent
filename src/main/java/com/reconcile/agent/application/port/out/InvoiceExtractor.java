package com.reconcile.agent.application.port.out;

import com.reconcile.agent.domain.model.Invoice;
import reactor.core.publisher.Mono;

public interface InvoiceExtractor {
    Mono<Invoice> extract(String rawText);
}
