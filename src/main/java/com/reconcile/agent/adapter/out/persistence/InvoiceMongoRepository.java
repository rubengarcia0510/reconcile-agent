package com.reconcile.agent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface InvoiceMongoRepository extends ReactiveMongoRepository<InvoicePersistenceAdapter.Doc, String> {
    Flux<InvoicePersistenceAdapter.Doc> findByRunId(String runId);
}
