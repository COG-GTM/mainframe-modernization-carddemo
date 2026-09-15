# CardDemo web UI

3270-style React + TypeScript + Vite front end for the online slice in `../src/online`
(`COSGN00C` signon, `COMEN01C` main menu, `COACTVWC` account view). Screen layout, captions,
field lengths and message lines mirror the BMS maps `app/bms/COSGN00.bms`, `app/bms/COMEN01.bms`
and `app/bms/COACTVW.bms`: every element is placed on a 24x80 grid by its `POS=(line,column)`.

## Running

```sh
cd ts-app && npm install && npm run serve   # Express API on :3000
cd ts-app/web && npm install && npm run dev # UI on :5173, proxied to the API
```

Sign on with any user from `ts-app/data/usrsec.txt`, e.g. `ADMIN001` / `PASSWORD`.
Set `CARDDEMO_API` to proxy to an API on another host or port.

## Notes

- Monetary fields arrive from the API as strings and are rendered verbatim — they are never
  parsed into JS numbers.
- A `401` from any request means the session is gone (CICS `EIBCALEN = 0`) and the UI returns to
  the signon screen.
