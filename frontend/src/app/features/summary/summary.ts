import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RunStore } from '../../state/run.store';
import { Skeleton } from '../../ui/skeleton';

@Component({
  selector: 'app-summary',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Skeleton],
  template: `
    @if (store.phase() !== 'idle') {
      <section class="stats" aria-label="Run summary">
        @if (store.phase() === 'done') {
          <div class="stat"><strong>{{ store.summary()?.invoicesProcessed }}</strong><span>Invoices read</span></div>
          <div class="stat"><strong>{{ store.summary()?.transactionsLoaded }}</strong><span>Bank rows</span></div>
          <div class="stat"><strong>{{ store.analysis()?.matched }}</strong><span>Matched</span></div>
          <div class="stat"><strong>{{ store.analysis()?.unmatchedInvoices }}</strong><span>Unmatched invoices</span></div>
          <div class="stat"><strong>{{ store.analysis()?.unmatchedTransactions }}</strong><span>Unmatched bank rows</span></div>
          <div class="stat warn"><strong>{{ store.pendingCount() }}</strong><span>Anomalies to review</span></div>
        } @else {
          @for (item of placeholders; track item) {
            <div class="stat">
              <app-skeleton width="3rem" height="1.6rem" />
              <app-skeleton width="7rem" height="0.8rem" />
            </div>
          }
        }
      </section>
      @if (store.summary(); as summary) {
        @if (summary.invoicesFailed > 0) {
          <p class="notice">{{ summary.invoicesFailed }} file(s) could not be read: {{ summary.failedFiles.join(', ') }}</p>
        }
      }
    }
  `,
})
export class Summary {
  protected readonly store = inject(RunStore);
  protected readonly placeholders = [1, 2, 3, 4, 5, 6];
}
