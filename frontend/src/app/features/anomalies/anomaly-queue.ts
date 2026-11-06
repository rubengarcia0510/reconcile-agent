import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AnomalyType } from '../../core/models';
import { RunStore } from '../../state/run.store';
import { Skeleton } from '../../ui/skeleton';

@Component({
  selector: 'app-anomaly-queue',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Skeleton],
  template: `
    @if (store.phase() !== 'idle') {
      <section class="card">
        <h2>2. Approval queue</h2>
        @if (store.busy()) {
          @for (item of placeholders; track item) {
            <article class="anomaly">
              <app-skeleton width="9rem" height="1.2rem" />
              <app-skeleton height="0.9rem" />
              <app-skeleton width="60%" height="0.9rem" />
            </article>
          }
        } @else {
          @for (anomaly of store.anomalies(); track anomaly.id) {
            <article class="anomaly" [class.resolved]="anomaly.status !== 'PENDING'">
              <header>
                <span class="badge" [attr.data-type]="anomaly.type">{{ label(anomaly.type) }}</span>
                <span class="status" [attr.data-status]="anomaly.status">{{ anomaly.status }}</span>
              </header>
              <p>{{ anomaly.explanation }}</p>
              @if (anomaly.status === 'PENDING') {
                <div class="actions">
                  <button type="button" class="primary" [disabled]="isBusy(anomaly.id)" (click)="store.review(anomaly.id, 'approve')">Approve</button>
                  <button type="button" class="danger" [disabled]="isBusy(anomaly.id)" (click)="store.review(anomaly.id, 'reject')">Reject</button>
                </div>
              }
            </article>
          } @empty {
            <p class="hint">No anomalies found. Everything reconciles.</p>
          }
        }
      </section>
    }
  `,
})
export class AnomalyQueue {
  protected readonly store = inject(RunStore);
  protected readonly placeholders = [1, 2, 3];

  private readonly labels: Record<AnomalyType, string> = {
    DUPLICATE_INVOICE: 'Duplicate invoice',
    UNUSUAL_AMOUNT: 'Unusual amount',
    AMOUNT_MISMATCH: 'Amount mismatch',
    PAYMENT_WITHOUT_INVOICE: 'Payment without invoice',
  };

  protected label(type: AnomalyType): string {
    return this.labels[type];
  }

  protected isBusy(anomalyId: string): boolean {
    return this.store.reviewing().includes(anomalyId);
  }
}
