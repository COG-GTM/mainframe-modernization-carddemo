           02  UTYPE08L    COMP  PIC  S9(4).
           02  UTYPE08F    PICTURE X.
           02  FILLER REDEFINES UTYPE08F.
             03 UTYPE08A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  UTYPE08I  PIC X(1).
           02  SEL0009L    COMP  PIC  S9(4).
           02  SEL0009F    PICTURE X.
           02  FILLER REDEFINES SEL0009F.
             03 SEL0009A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SEL0009I  PIC X(1).
           02  USRID09L    COMP  PIC  S9(4).
           02  USRID09F    PICTURE X.
           02  FILLER REDEFINES USRID09F.
             03 USRID09A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  USRID09I  PIC X(8).
           02  FNAME09L    COMP  PIC  S9(4).
           02  FNAME09F    PICTURE X.
           02  FILLER REDEFINES FNAME09F.
             03 FNAME09A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  FNAME09I  PIC X(20).
           02  LNAME09L    COMP  PIC  S9(4).
           02  LNAME09F    PICTURE X.
           02  FILLER REDEFINES LNAME09F.
             03 LNAME09A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  LNAME09I  PIC X(20).
           02  UTYPE09L    COMP  PIC  S9(4).
           02  UTYPE09F    PICTURE X.
           02  FILLER REDEFINES UTYPE09F.
             03 UTYPE09A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  UTYPE09I  PIC X(1).
           02  SEL0010L    COMP  PIC  S9(4).
           02  SEL0010F    PICTURE X.
           02  FILLER REDEFINES SEL0010F.
             03 SEL0010A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SEL0010I  PIC X(1).
           02  USRID10L    COMP  PIC  S9(4).
           02  USRID10F    PICTURE X.
           02  FILLER REDEFINES USRID10F.
             03 USRID10A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  USRID10I  PIC X(8).
           02  FNAME10L    COMP  PIC  S9(4).
           02  FNAME10F    PICTURE X.
           02  FILLER REDEFINES FNAME10F.
             03 FNAME10A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  FNAME10I  PIC X(20).
           02  LNAME10L    COMP  PIC  S9(4).
           02  LNAME10F    PICTURE X.
           02  FILLER REDEFINES LNAME10F.
             03 LNAME10A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  LNAME10I  PIC X(20).
           02  UTYPE10L    COMP  PIC  S9(4).
           02  UTYPE10F    PICTURE X.
           02  FILLER REDEFINES UTYPE10F.
             03 UTYPE10A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  UTYPE10I  PIC X(1).
           02  ERRMSGL    COMP  PIC  S9(4).
           02  ERRMSGF    PICTURE X.
           02  FILLER REDEFINES ERRMSGF.
             03 ERRMSGA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ERRMSGI  PIC X(78).
       01  COUSR0AO REDEFINES COUSR0AI.
           02  FILLER PIC X(12).
           02  FILLER PICTURE X(3).
           02  TRNNAMEC    PICTURE X.
           02  TRNNAMEP    PICTURE X.
           02  TRNNAMEH    PICTURE X.
           02  TRNNAMEV    PICTURE X.
           02  TRNNAMEO  PIC X(4).
           02  FILLER PICTURE X(3).
           02  TITLE01C    PICTURE X.
           02  TITLE01P    PICTURE X.
           02  TITLE01H    PICTURE X.
           02  TITLE01V    PICTURE X.
           02  TITLE01O  PIC X(40).
           02  FILLER PICTURE X(3).
           02  CURDATEC    PICTURE X.
           02  CURDATEP    PICTURE X.
           02  CURDATEH    PICTURE X.
           02  CURDATEV    PICTURE X.
           02  CURDATEO  PIC X(8).
           02  FILLER PICTURE X(3).
           02  PGMNAMEC    PICTURE X.
           02  PGMNAMEP    PICTURE X.
           02  PGMNAMEH    PICTURE X.
           02  PGMNAMEV    PICTURE X.
           02  PGMNAMEO  PIC X(8).
           02  FILLER PICTURE X(3).
           02  TITLE02C    PICTURE X.
