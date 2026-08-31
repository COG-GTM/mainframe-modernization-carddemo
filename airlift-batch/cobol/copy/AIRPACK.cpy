      *****************************************************************
      * AIRPACK - harness-owned layout used to pin the byte encoding of
      * the usages the codec supports. The slice records in app/cpy are
      * all USAGE DISPLAY, while the estate's COMP-3 usage sits in
      * WORKING-STORAGE (CBSTM03A:196 WS-TOTAL-AMT PIC S9(9)V99 COMP-3,
      * CBTRN03C:151-152 packed counters), so packed and binary support is
      * exercised against vectors written by GnuCOBOL itself rather than
      * against the slice.
      *****************************************************************
       01  AIRPACK-RECORD.
           05  AP-LABEL                PIC X(16).
           05  AP-ZONED-SIGNED         PIC S9(09)V99.
           05  AP-ZONED-UNSIGNED       PIC 9(07).
           05  AP-COMP3-SIGNED         PIC S9(09)V99 COMP-3.
           05  AP-COMP3-UNSIGNED       PIC 9(05)V999 COMP-3.
           05  AP-COMP-HALF            PIC S9(04) COMP.
           05  AP-COMP-FULL            PIC S9(09) COMP.
