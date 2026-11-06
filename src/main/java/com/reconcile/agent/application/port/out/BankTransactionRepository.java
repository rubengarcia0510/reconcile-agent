package com.reconcile.agent.application.port.out;

import com.reconcile.agent.domain.model.BankTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankTransactionRepository {
    Mono<BankTransaction> save(BankTransaction transaction);

    Flux<BankTransaction> findByRunId(String runId);
}
