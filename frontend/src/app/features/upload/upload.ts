import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RunStore } from '../../state/run.store';
import { Skeleton } from '../../ui/skeleton';

@Component({
  selector: 'app-upload',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Skeleton],
  template: `
    <section class="card">
      <h2>1. Upload documents</h2>
      <label class="field">
        <span>Invoices (PDF, up to 30)</span>
        <input type="file" accept="application/pdf" multiple [disabled]="store.busy()" (change)="onInvoices($event)" />
      </label>
      <label class="field">
        <span>Bank statement (CSV: date, description, amount, currency, reference)</span>
        <input type="file" accept=".csv,text/csv" [disabled]="store.busy()" (change)="onStatement($event)" />
      </label>
      @if (store.busy()) {
        <div class="progress" role="status" aria-live="polite">
          <p class="hint">{{ statusText() }}</p>
          <app-skeleton height="0.9rem" />
          <app-skeleton width="70%" height="0.9rem" />
        </div>
      } @else {
        <div class="actions">
          <button type="button" class="primary" [disabled]="!canStart()" (click)="start()">Analyze</button>
          @if (store.phase() === 'done') {
            <button type="button" (click)="store.reset()">Start over</button>
          }
        </div>
      }
    </section>
  `,
})
export class Upload {
  protected readonly store = inject(RunStore);
  private readonly invoices = signal<File[]>([]);
  private readonly statement = signal<File | null>(null);

  protected readonly canStart = computed(() => this.invoices().length > 0 && this.statement() !== null);
  protected readonly statusText = computed(() =>
    this.store.phase() === 'uploading'
      ? 'Reading the invoices with AI and loading the bank statement...'
      : 'Reconciling payments and looking for anomalies...',
  );

  protected onInvoices(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.invoices.set(Array.from(input.files ?? []));
  }

  protected onStatement(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.statement.set(input.files?.item(0) ?? null);
  }

  protected start(): void {
    const statement = this.statement();
    if (statement) {
      void this.store.startRun(this.invoices(), statement);
    }
  }
}
