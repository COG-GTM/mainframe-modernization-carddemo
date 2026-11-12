      * Test double for the EXEC interface block the CICS translator adds.
       01  DFHEIBLK.
           05  EIBTIME     PIC S9(7) COMP-3.
           05  EIBDATE     PIC S9(7) COMP-3.
           05  EIBTRNID    PIC X(4).
           05  EIBTASKN    PIC S9(7) COMP-3.
           05  EIBTRMID    PIC X(4).
           05  DFHEIGDI    PIC S9(4) COMP.
           05  EIBCPOSN    PIC S9(4) COMP.
           05  EIBCALEN    PIC S9(4) COMP.
           05  EIBAID      PIC X.
           05  EIBFN       PIC X(2).
           05  EIBRCODE    PIC X(6).
           05  EIBDS       PIC X(8).
           05  EIBREQID    PIC X(8).
           05  EIBRSRCE    PIC X(8).
           05  FILLER      PIC X(16).
           05  EIBRESP     PIC S9(8) COMP.
           05  EIBRESP2    PIC S9(8) COMP.
           05  EIBRLDBK    PIC X.
