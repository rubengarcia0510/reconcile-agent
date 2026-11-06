import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AnalysisResult, Anomaly, Invoice, ReviewDecision, RunSummary } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  startRun(invoices: File[], statement: File): Observable<RunSummary> {
    const body = new FormData();
    invoices.forEach((file) => body.append('invoices', file, file.name));
    body.append('statement', statement, statement.name);
    return this.http.post<RunSummary>('/api/runs', body);
  }

  listInvoices(runId: string): Observable<Invoice[]> {
    return this.http.get<Invoice[]>('/api/invoices', { params: { runId } });
  }

  analyze(runId: string): Observable<AnalysisResult> {
    return this.http.post<AnalysisResult>(`/api/runs/${runId}/analyze`, null);
  }

  review(anomalyId: string, decision: ReviewDecision): Observable<Anomaly> {
    return this.http.post<Anomaly>(`/api/anomalies/${anomalyId}/${decision}`, null);
  }
}

export function messageOf(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as { error?: string } | null;
    return body?.error ?? `Request failed (${error.status})`;
  }
  return error instanceof Error ? error.message : 'Unexpected error';
}
