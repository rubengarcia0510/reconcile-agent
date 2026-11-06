import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AnomalyQueue } from './features/anomalies/anomaly-queue';
import { InvoiceTable } from './features/invoices/invoice-table';
import { Summary } from './features/summary/summary';
import { Upload } from './features/upload/upload';
import { RunStore } from './state/run.store';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Upload, Summary, AnomalyQueue, InvoiceTable],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly store = inject(RunStore);
}
