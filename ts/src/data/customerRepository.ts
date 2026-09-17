import { CUSTOMER_LAYOUT, CustomerRecord } from '../models/customer';
import { KsdsFile } from './ksds';

/** CUSTDATA (MFE.CARDDEMO.CUSTDATA, CICS file CUSTDAT) keyed on CUST-ID(9). */
export function customerKey(custId: number | string): string {
  return String(custId).trim().padStart(9, '0');
}

export class CustomerRepository {
  readonly file = new KsdsFile<CustomerRecord>({
    name: 'CUSTDAT',
    layout: CUSTOMER_LAYOUT,
    keyOf: (record) => customerKey(record.custId),
  });

  load(records: Iterable<CustomerRecord>): void {
    this.file.load(records);
  }

  read(custId: number | string): CustomerRecord {
    return this.file.read(customerKey(custId));
  }

  tryRead(custId: number | string): CustomerRecord | undefined {
    return this.file.tryRead(customerKey(custId));
  }

  readAll(): CustomerRecord[] {
    return this.file.readAll();
  }

  write(record: CustomerRecord): void {
    this.file.write(record);
  }

  rewrite(record: CustomerRecord): void {
    this.file.rewrite(record);
  }

  delete(custId: number | string): void {
    this.file.delete(customerKey(custId));
  }

  startBrowse(custId?: number | string) {
    return this.file.startBrowse(custId === undefined ? undefined : customerKey(custId));
  }
}
