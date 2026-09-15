import { useState } from 'react';
import { ApiError, signon, type SignonResponse } from '../api.js';
import { Footer, Header } from '../screen/Header.js';
import { Input, Screen, Text } from '../screen/Screen.js';

/** The banknote artwork at POS=(7..15,21) in `app/bms/COSGN00.bms`. */
const BANKNOTE = [
  '+========================================+',
  '|%%%%%%%  NATIONAL RESERVE NOTE  %%%%%%%%|',
  '|%(1)  THE UNITED STATES OF KICSLAND (1)%|',
  '|%$$              ___       ********  $$%|',
  '|%$    {x}       (o o)                 $%|',
  '|%$     ******  (  V  )      O N E     $%|',
  '|%(1)          ---m-m---             (1)%|',
  '|%%~~~~~~~~~~~ ONE DOLLAR ~~~~~~~~~~~~~%%|',
  '+========================================+',
];

export function SignonScreen({
  now,
  onSignedOn,
}: {
  now: Date;
  onSignedOn: (response: SignonResponse) => void;
}): JSX.Element {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  async function submit(): Promise<void> {
    setErrorMessage('');
    try {
      onSignedOn(await signon(userId, password));
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : String(error));
    }
  }

  return (
    <Screen onEnter={() => void submit()}>
      <Header tranId="CC00" program="COSGN00C" now={now} spaced />
      <Text row={3} col={1} len={6}>
        AppID:
      </Text>
      <Text row={3} col={8} len={8}>
        CICSCDMO
      </Text>
      <Text row={3} col={64} len={6}>
        SysID:
      </Text>
      <Text row={3} col={71} len={8}>
        CDMO
      </Text>

      <Text row={5} col={6} len={66} color="neutral">
        This is a Credit Card Demo Application for Mainframe Modernization
      </Text>

      {BANKNOTE.map((line, index) => (
        <Text key={line} row={7 + index} col={21} len={42}>
          {line}
        </Text>
      ))}

      <Text row={17} col={16} len={49} color="turquoise">
        Type your User ID and Password, then press ENTER:
      </Text>

      <Text row={19} col={29} len={13} color="turquoise">
        {'User ID     :'}
      </Text>
      <Input
        id="userId"
        label="User ID"
        row={19}
        col={43}
        len={8}
        value={userId}
        onChange={setUserId}
        autoFocus
      />
      <Text row={19} col={52} len={8}>
        (8 Char)
      </Text>

      <Text row={20} col={29} len={13} color="turquoise">
        {'Password    :'}
      </Text>
      <Input
        id="password"
        label="Password"
        row={20}
        col={43}
        len={8}
        value={password}
        onChange={setPassword}
        password
      />
      <Text row={20} col={52} len={8}>
        (8 Char)
      </Text>

      <Footer errorMessage={errorMessage} keys="ENTER=Sign-on  F3=Exit" />
    </Screen>
  );
}
