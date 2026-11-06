package com.reconcile.agent.application.port.out;

import com.reconcile.agent.domain.model.BankTransaction;
import reactor.core.publisher.Flux;

public interface BankStatementParser {
    Flux<BankTransaction> parse(byte[] content, String runId);
}
