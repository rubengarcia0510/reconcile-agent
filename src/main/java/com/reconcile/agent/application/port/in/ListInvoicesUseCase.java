package com.reconcile.agent.application.port.in;

import com.reconcile.agent.domain.model.Invoice;
import reactor.core.publisher.Flux;

public interface ListInvoicesUseCase {
    Flux<Invoice> list(String runId);
}
