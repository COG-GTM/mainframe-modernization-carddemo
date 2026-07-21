import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { BillPaymentComponent } from './bill-payment.component';
import { BillPaymentResponse } from './bill-payment.model';

describe('BillPaymentComponent', () => {
  let fixture: ComponentFixture<BillPaymentComponent>;
  let component: BillPaymentComponent;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BillPaymentComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(BillPaymentComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  function flushPay(body: Partial<BillPaymentResponse>): void {
    const req = httpMock.expectOne('http://localhost:8080/api/billpay/pay');
    req.flush({
      messageType: 'INFO', message: '', balance: null, balanceDisplay: '',
      accountId: null, transactionId: null, fieldInError: 'NONE', cleared: false,
      ...body
    });
  }

  it('shows the confirm prompt and balance on inquiry (blank confirm)', () => {
    component.acctId = '11';
    component.confirm = '';
    component.onEnter();
    flushPay({ messageType: 'INFO', message: 'Confirm to make a bill payment...', balanceDisplay: '+0000000123.45' });
    expect(component.message).toBe('Confirm to make a bill payment...');
    expect(component.balanceDisplay).toBe('+0000000123.45');
  });

  it('renders success in green and clears inputs after payment', () => {
    component.acctId = '11';
    component.confirm = 'Y';
    component.onEnter();
    flushPay({ messageType: 'SUCCESS', message: 'Payment successful.  Your Transaction ID is 0000000000000001.', balanceDisplay: '+0000000000.00' });
    expect(component.messageType).toBe('SUCCESS');
    expect(component.acctId).toBe('');
    expect(component.confirm).toBe('');
  });

  it('clears the screen when payment is declined (cleared response)', () => {
    component.acctId = '11';
    component.confirm = 'N';
    component.balanceDisplay = '+0000000123.45';
    component.onEnter();
    flushPay({ cleared: true });
    expect(component.acctId).toBe('');
    expect(component.balanceDisplay).toBe('');
    expect(component.message).toBe('');
  });

  it('F4/onClear resets all fields', () => {
    component.acctId = '11';
    component.balanceDisplay = '+0000000123.45';
    component.message = 'x';
    component.onClear();
    expect(component.acctId).toBe('');
    expect(component.balanceDisplay).toBe('');
    expect(component.message).toBe('');
  });
});
