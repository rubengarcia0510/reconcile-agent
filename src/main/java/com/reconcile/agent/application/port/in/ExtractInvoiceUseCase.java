package com.reconcile.agent.application.port.in;

import com.reconcile.agent.domain.model.Invoice;
import reactor.core.publisher.Mono;

public interface ExtractInvoiceUseCase {
    Mono<Invoice> extract(String rawText);
}
