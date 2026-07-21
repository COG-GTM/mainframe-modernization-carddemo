import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BillPaymentResponse } from './bill-payment.model';

/** Client for the migrated Bill Payment (CB00 / COBIL00C) REST endpoints. */
@Injectable({ providedIn: 'root' })
export class BillPaymentService {
  private readonly baseUrl = 'http://localhost:8080/api/billpay';

  constructor(private http: HttpClient) {}

  inquiry(acctId: string): Observable<BillPaymentResponse> {
    return this.http.post<BillPaymentResponse>(`${this.baseUrl}/inquiry`, { acctId });
  }

  pay(acctId: string, confirm: string): Observable<BillPaymentResponse> {
    return this.http.post<BillPaymentResponse>(`${this.baseUrl}/pay`, { acctId, confirm });
  }
}
