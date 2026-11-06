import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    'aria-hidden': 'true',
    '[style.width]': 'width()',
    '[style.height]': 'height()',
  },
  template: '',
  styles: `
    :host {
      display: block;
      border-radius: 6px;
      background: linear-gradient(90deg, var(--skeleton-a) 25%, var(--skeleton-b) 50%, var(--skeleton-a) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.2s ease-in-out infinite;
    }
    @keyframes shimmer {
      from { background-position: 200% 0; }
      to { background-position: -200% 0; }
    }
  `,
})
export class Skeleton {
  readonly width = input('100%');
  readonly height = input('1rem');
}
