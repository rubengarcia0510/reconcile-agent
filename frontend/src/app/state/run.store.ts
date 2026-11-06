import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { firstValueFrom } from 'rxjs';
import { ApiService, messageOf } from '../core/api.service';
import { AnalysisResult, Anomaly, Invoice, ReviewDecision, RunSummary } from '../core/models';

type Phase = 'idle' | 'uploading' | 'analyzing' | 'done';

interface RunState {
  phase: Phase;
  runId: string | null;
  summary: RunSummary | null;
  invoices: Invoice[];
  analysis: AnalysisResult | null;
  anomalies: Anomaly[];
  reviewing: string[];
  error: string | null;
}

const initialState: RunState = {
  phase: 'idle',
  runId: null,
  summary: null,
  invoices: [],
  analysis: null,
  anomalies: [],
  reviewing: [],
  error: null,
};

export const RunStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withComputed(({ anomalies, phase }) => ({
    pendingCount: computed(() => anomalies().filter((a) => a.status === 'PENDING').length),
    busy: computed(() => phase() === 'uploading' || phase() === 'analyzing'),
    flaggedInvoiceIds: computed(
      () => new Set(anomalies().map((a) => a.invoiceId).filter((id): id is string => id !== null)),
    ),
  })),
  withMethods((store, api = inject(ApiService)) => ({
    async startRun(invoiceFiles: File[], statement: File): Promise<void> {
      patchState(store, { ...initialState, phase: 'uploading' });
      try {
        const summary = await firstValueFrom(api.startRun(invoiceFiles, statement));
        patchState(store, { runId: summary.runId, summary, phase: 'analyzing' });
        const [invoices, analysis] = await Promise.all([
          firstValueFrom(api.listInvoices(summary.runId)),
          firstValueFrom(api.analyze(summary.runId)),
        ]);
        patchState(store, { invoices, analysis, anomalies: analysis.anomalies, phase: 'done' });
      } catch (error) {
        patchState(store, { phase: 'idle', error: messageOf(error) });
      }
    },

    async review(anomalyId: string, decision: ReviewDecision): Promise<void> {
      patchState(store, (state) => ({ reviewing: [...state.reviewing, anomalyId], error: null }));
      try {
        const updated = await firstValueFrom(api.review(anomalyId, decision));
        patchState(store, (state) => ({
          anomalies: state.anomalies.map((a) => (a.id === updated.id ? updated : a)),
        }));
      } catch (error) {
        patchState(store, { error: messageOf(error) });
      } finally {
        patchState(store, (state) => ({ reviewing: state.reviewing.filter((id) => id !== anomalyId) }));
      }
    },

    reset(): void {
      patchState(store, initialState);
    },
  })),
);
