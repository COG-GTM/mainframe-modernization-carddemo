import { Component } from '@angular/core';
import { BillPaymentComponent } from './bill-payment/bill-payment.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BillPaymentComponent],
  template: '<app-bill-payment></app-bill-payment>'
})
export class AppComponent {
  title = 'CardDemo Bill Payment';
}
