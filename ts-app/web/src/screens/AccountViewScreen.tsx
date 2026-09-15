import { useState } from 'react';
import { ApiError, viewAccount, type AccountViewResponse } from '../api.js';
import { Footer, Header } from '../screen/Header.js';
import { Input, Screen, Text } from '../screen/Screen.js';

/**
 * Money arrives from the API already formatted by `serializeRecord`; it is
 * displayed verbatim so no precision is lost to a JS number.
 */
function money(value: string | undefined): string {
  return value ?? '';
}

export function AccountViewScreen({
  now,
  sessionId,
  onSessionExpired,
}: {
  now: Date;
  sessionId: string;
  onSessionExpired: () => void;
}): JSX.Element {
  const [acctId, setAcctId] = useState('');
  const [data, setData] = useState<AccountViewResponse | undefined>(undefined);
  const [errorMessage, setErrorMessage] = useState('');

  const account = data?.account ?? {};
  const customer = data?.customer ?? {};

  async function submit(): Promise<void> {
    setErrorMessage('');
    try {
      setData(await viewAccount(sessionId, acctId));
    } catch (error) {
      setData(undefined);
      if (error instanceof ApiError) setErrorMessage(error.message);
      else onSessionExpired();
    }
  }

  return (
    <Screen onEnter={() => void submit()}>
      <Header tranId="CAVW" program="COACTVWC" now={now} />
      <Text row={4} col={33} len={12} color="neutral">
        View Account
      </Text>

      <Text row={5} col={19} len={16} color="turquoise">
        Account Number :
      </Text>
      <Input
        id="acctId"
        label="Account Number"
        row={5}
        col={38}
        len={11}
        value={acctId}
        onChange={setAcctId}
        autoFocus
        numeric
      />
      <Text row={5} col={57} len={12} color="turquoise">
        {'Active Y/N: '}
      </Text>
      <Text row={5} col={70} len={1} color="green" underline>
        {account.acctActiveStatus ?? ''}
      </Text>

      <Text row={6} col={8} len={7} color="turquoise">
        Opened:
      </Text>
      <Text row={6} col={17} len={10} color="green" underline>
        {account.acctOpenDate ?? ''}
      </Text>
      <Text row={6} col={39} len={21} color="turquoise">
        {'Credit Limit        :'}
      </Text>
      <Text row={6} col={61} len={15} color="green" underline align="right">
        {money(account.acctCreditLimit)}
      </Text>

      <Text row={7} col={8} len={7} color="turquoise">
        Expiry:
      </Text>
      <Text row={7} col={17} len={10} color="green" underline>
        {account.acctExpiraionDate ?? ''}
      </Text>
      <Text row={7} col={39} len={21} color="turquoise">
        {'Cash credit Limit   :'}
      </Text>
      <Text row={7} col={61} len={15} color="green" underline align="right">
        {money(account.acctCashCreditLimit)}
      </Text>

      <Text row={8} col={8} len={8} color="turquoise">
        Reissue:
      </Text>
      <Text row={8} col={17} len={10} color="green" underline>
        {account.acctReissueDate ?? ''}
      </Text>
      <Text row={8} col={39} len={21} color="turquoise">
        {'Current Balance     :'}
      </Text>
      <Text row={8} col={61} len={15} color="green" underline align="right">
        {money(account.acctCurrBal)}
      </Text>

      <Text row={9} col={39} len={21} color="turquoise">
        Current Cycle Credit:
      </Text>
      <Text row={9} col={61} len={15} color="green" underline align="right">
        {money(account.acctCurrCycCredit)}
      </Text>

      <Text row={10} col={8} len={14} color="turquoise">
        Account Group:
      </Text>
      <Text row={10} col={23} len={10} color="green" underline>
        {account.acctGroupId ?? ''}
      </Text>
      <Text row={10} col={39} len={21} color="turquoise">
        {'Current Cycle Debit :'}
      </Text>
      <Text row={10} col={61} len={15} color="green" underline align="right">
        {money(account.acctCurrCycDebit)}
      </Text>

      <Text row={11} col={32} len={16} color="neutral">
        Customer Details
      </Text>

      <Text row={12} col={8} len={14} color="turquoise">
        {'Customer id  :'}
      </Text>
      <Text row={12} col={23} len={9} color="green" underline>
        {customer.custId ?? ''}
      </Text>
      <Text row={12} col={49} len={4} color="turquoise">
        SSN:
      </Text>
      <Text row={12} col={54} len={12} color="green" underline>
        {customer.custSsn ?? ''}
      </Text>

      <Text row={13} col={8} len={14} color="turquoise">
        Date of birth:
      </Text>
      <Text row={13} col={23} len={10} color="green" underline>
        {customer.custDobYyyyMmDd ?? ''}
      </Text>
      <Text row={13} col={49} len={11} color="turquoise">
        FICO Score:
      </Text>
      <Text row={13} col={61} len={3} color="green" underline>
        {customer.custFicoCreditScore ?? ''}
      </Text>

      <Text row={14} col={1} len={10} color="turquoise">
        First Name
      </Text>
      <Text row={14} col={28} len={13} color="turquoise">
        {'Middle Name: '}
      </Text>
      <Text row={14} col={55} len={12} color="turquoise">
        {'Last Name : '}
      </Text>
      <Text row={15} col={1} len={25} color="green" underline>
        {customer.custFirstName ?? ''}
      </Text>
      <Text row={15} col={28} len={25} color="green" underline>
        {customer.custMiddleName ?? ''}
      </Text>
      <Text row={15} col={55} len={25} color="green" underline>
        {customer.custLastName ?? ''}
      </Text>

      <Text row={16} col={1} len={8} color="turquoise">
        Address:
      </Text>
      <Text row={16} col={10} len={50} color="green" underline>
        {customer.custAddrLine1 ?? ''}
      </Text>
      <Text row={16} col={63} len={6} color="turquoise">
        {'State '}
      </Text>
      <Text row={16} col={73} len={2} color="green" underline>
        {customer.custAddrStateCd ?? ''}
      </Text>

      <Text row={17} col={10} len={50} color="green" underline>
        {customer.custAddrLine2 ?? ''}
      </Text>
      <Text row={17} col={63} len={3} color="turquoise">
        Zip
      </Text>
      <Text row={17} col={73} len={5} color="green" underline align="right">
        {customer.custAddrZip ?? ''}
      </Text>

      <Text row={18} col={1} len={5} color="turquoise">
        {'City '}
      </Text>
      <Text row={18} col={10} len={50} color="green" underline>
        {customer.custAddrLine3 ?? ''}
      </Text>
      <Text row={18} col={63} len={7} color="turquoise">
        Country
      </Text>
      <Text row={18} col={73} len={3} color="green" underline>
        {customer.custAddrCountryCd ?? ''}
      </Text>

      <Text row={19} col={1} len={8} color="turquoise">
        Phone 1:
      </Text>
      <Text row={19} col={10} len={13} color="green" underline>
        {customer.custPhoneNum1 ?? ''}
      </Text>
      <Text row={19} col={24} len={30} color="turquoise">
        {'Government Issued Id Ref    : '}
      </Text>
      <Text row={19} col={58} len={20} color="green" underline>
        {customer.custGovtIssuedId ?? ''}
      </Text>

      <Text row={20} col={1} len={8} color="turquoise">
        Phone 2:
      </Text>
      <Text row={20} col={10} len={13} color="green" underline>
        {customer.custPhoneNum2 ?? ''}
      </Text>
      <Text row={20} col={24} len={16} color="turquoise">
        {'EFT Account Id: '}
      </Text>
      <Text row={20} col={41} len={10} color="green" underline>
        {customer.custEftAccountId ?? ''}
      </Text>
      <Text row={20} col={53} len={24} color="turquoise">
        Primary Card Holder Y/N:
      </Text>
      <Text row={20} col={78} len={1} color="green" underline>
        {customer.custPriCardHolderInd ?? ''}
      </Text>

      <Text row={22} col={23} len={45} color="neutral">
        {data?.infoMessage ?? 'Enter or update id of account to display'}
      </Text>

      <Footer errorMessage={errorMessage} keys="  F3=Exit " />
    </Screen>
  );
}
