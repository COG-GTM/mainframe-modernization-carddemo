import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BillPaymentService } from './bill-payment.service';
import { BillPaymentResponse } from './bill-payment.model';

/**
 * Bill Payment screen — migrated from BMS map COBIL0A (mapset COBIL00) / program COBIL00C.
 * Layout and behavior mirror the 3270 screen: Acct ID + Confirm inputs, current balance,
 * a message line (RED for errors, GREEN on success), and ENTER / F3 / F4 keys.
 */
@Component({
  selector: 'app-bill-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bill-payment.component.html',
  styleUrl: './bill-payment.component.css'
})
export class BillPaymentComponent implements OnInit {
  tranId = 'CB00';
  progName = 'COBIL00C';
  title = 'Bill Payment';
  curDate = '';
  curTime = '';
  separatorLine = '-'.repeat(78);

  acctId = '';
  confirm = '';
  balanceDisplay = '';
  message = '';
  messageType: 'SUCCESS' | 'ERROR' | 'INFO' | '' = '';

  constructor(private billPay: BillPaymentService) {}

  ngOnInit(): void {
    this.updateClock();
    setInterval(() => this.updateClock(), 1000);
  }

  private updateClock(): void {
    const now = new Date();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const yy = String(now.getFullYear()).slice(2);
    this.curDate = `${mm}/${dd}/${yy}`;
    const hh = String(now.getHours()).padStart(2, '0');
    const mi = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    this.curTime = `${hh}:${mi}:${ss}`;
  }

  @HostListener('window:keydown', ['$event'])
  handleKey(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.onEnter();
    } else if (event.key === 'F3') {
      event.preventDefault();
      this.onBack();
    } else if (event.key === 'F4') {
      event.preventDefault();
      this.onClear();
    }
  }

  onEnter(): void {
    // Mirrors COBIL00C PROCESS-ENTER-KEY: one path, driven by the confirm value.
    this.billPay.pay(this.acctId, this.confirm).subscribe({
      next: (r) => this.applyResponse(r),
      error: () => {
        this.messageType = 'ERROR';
        this.message = 'Unable to reach Bill Payment service.';
      }
    });
  }

  private applyResponse(r: BillPaymentResponse): void {
    if (r.cleared) {
      this.clearFields();
      return;
    }
    this.messageType = r.messageType;
    this.message = r.message;
    this.balanceDisplay = r.balanceDisplay || this.balanceDisplay;
    if (r.messageType === 'SUCCESS') {
      // COBOL clears the input fields after a successful payment (INITIALIZE-ALL-FIELDS).
      this.acctId = '';
      this.confirm = '';
    }
  }

  onClear(): void {
    this.clearFields();
  }

  private clearFields(): void {
    this.acctId = '';
    this.confirm = '';
    this.balanceDisplay = '';
    this.message = '';
    this.messageType = '';
  }

  onBack(): void {
    this.messageType = 'INFO';
    this.message = 'F3=Back: returning to the previous menu (COMEN01C).';
  }
}
