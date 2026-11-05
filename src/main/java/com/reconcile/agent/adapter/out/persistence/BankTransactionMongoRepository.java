package com.reconcile.agent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface BankTransactionMongoRepository
        extends ReactiveMongoRepository<BankTransactionPersistenceAdapter.Doc, String> {
    Flux<BankTransactionPersistenceAdapter.Doc> findByRunId(String runId);
}
