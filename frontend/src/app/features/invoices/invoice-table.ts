import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RunStore } from '../../state/run.store';
import { Skeleton } from '../../ui/skeleton';

@Component({
  selector: 'app-invoice-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, Skeleton],
  template: `
    @if (store.phase() !== 'idle') {
      <section class="card">
        <h2>3. Invoices read by the agent</h2>
        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>Number</th><th>Supplier</th><th>Issued</th><th class="num">Amount</th></tr>
            </thead>
            <tbody>
              @if (store.busy()) {
                @for (row of placeholders; track row) {
                  <tr>
                    <td><app-skeleton width="7rem" /></td>
                    <td><app-skeleton width="10rem" /></td>
                    <td><app-skeleton width="5rem" /></td>
                    <td><app-skeleton width="6rem" /></td>
                  </tr>
                }
              } @else {
                @for (invoice of store.invoices(); track invoice.id) {
                  <tr [class.flagged]="store.flaggedInvoiceIds().has(invoice.id)">
                    <td>{{ invoice.number }}</td>
                    <td>{{ invoice.supplier }}</td>
                    <td>{{ invoice.issueDate }}</td>
                    <td class="num">{{ invoice.amount | number: '1.2-2' }} {{ invoice.currency }}</td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>
      </section>
    }
  `,
})
export class InvoiceTable {
  protected readonly store = inject(RunStore);
  protected readonly placeholders = [1, 2, 3, 4, 5];
}
