package com.reconcile.agent.application.port.in;

import com.reconcile.agent.domain.model.RunSummary;
import java.util.List;
import reactor.core.publisher.Mono;

public interface StartRunUseCase {
    Mono<RunSummary> start(List<UploadedDocument> invoiceFiles, UploadedDocument statement);
}
