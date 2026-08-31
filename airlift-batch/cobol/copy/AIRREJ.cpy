      *****************************************************************
      * AIRREJ - the reject record CBTRN02C writes, which has no
      * copybook of its own in app/cpy: CBTRN02C:176-182 declares
      *     01 REJECT-RECORD.
      *        05 REJECT-TRAN-DATA          PIC X(350).
      *        05 VALIDATION-TRAILER        PIC X(80).
      * and fills the trailer from WS-VALIDATION-TRAILER, whose two
      * subordinate items are the reason code and its description. The
      * first 350 bytes are a DALYTRAN-RECORD (CBTRN02C:447), so the
      * comparator reads them through CVTRA06Y and only takes the
      * trailer fields from here.
      *****************************************************************
       01  AIRREJ-RECORD.
           05  AIRREJ-TRAN-DATA            PIC X(350).
           05  AIRREJ-FAIL-REASON          PIC 9(04).
           05  AIRREJ-FAIL-REASON-DESC     PIC X(76).
