           88  DID-NOT-FIND-CUST-IN-CUSTDAT        VALUE
               'Did not find associated customer in master file'.
           88  ACCT-STATUS-MUST-BE-YES-NO          VALUE
               'Account Active Status must be Y or N'.
           88  CRED-LIMIT-IS-BLANK                 VALUE
               'Credit Limit must be supplied'.
           88  CRED-LIMIT-IS-NOT-VALID             VALUE
               'Credit Limit is not valid'.
           88  THIS-MONTH-NOT-VALID                VALUE
               'Card expiry month must be between 1 and 12'.
           88  THIS-YEAR-NOT-VALID                 VALUE
               'Invalid card expiry year'.
           88  DID-NOT-FIND-ACCT-IN-CARDXREF       VALUE
               'Did not find this account in cards database'.
           88  DID-NOT-FIND-ACCTCARD-COMBO         VALUE
               'Did not find cards for this search condition'.
           88  COULD-NOT-LOCK-ACCT-FOR-UPDATE      VALUE
               'Could not lock account record for update'.
           88  COULD-NOT-LOCK-CUST-FOR-UPDATE      VALUE
               'Could not lock customer record for update'.
           88  DATA-WAS-CHANGED-BEFORE-UPDATE      VALUE
               'Record changed by some one else. Please review'.
           88  LOCKED-BUT-UPDATE-FAILED            VALUE
               'Update of record failed'.
           88  XREF-READ-ERROR                     VALUE
               'Error reading Card Data File'.
           88  CODING-TO-BE-DONE                   VALUE
               'Looks Good.... so far'.
      ******************************************************************
      *      Literals and Constants
      ******************************************************************
       01 WS-LITERALS.
          05 LIT-THISPGM                           PIC X(8)
                                                   VALUE 'COACTUPC'.
          05 LIT-THISTRANID                        PIC X(4)
                                                   VALUE 'CAUP'.
          05 LIT-THISMAPSET                        PIC X(8)
                                                   VALUE 'COACTUP '.
          05 LIT-THISMAP                           PIC X(7)
                                                   VALUE 'CACTUPA'.
          05 LIT-CARDUPDATE-PGM                    PIC X(8)
                                                   VALUE 'COCRDUPC'.
          05 LIT-CARDUPDATE-TRANID                 PIC X(4)
                                                   VALUE 'CCUP'.
          05 LIT-CARDUPDATE-MAPSET                 PIC X(8)
                                                   VALUE 'COCRDUP '.
          05 LIT-CARDUPDATE-MAP                    PIC X(7)
                                                   VALUE 'CCRDUPA'.
          05 LIT-CCLISTPGM                         PIC X(8)
                                                   VALUE 'COCRDLIC'.
          05 LIT-CCLISTTRANID                      PIC X(4)
                                                   VALUE 'CCLI'.
          05 LIT-CCLISTMAPSET                      PIC X(7)
                                                   VALUE 'COCRDLI'.
          05 LIT-CCLISTMAP                         PIC X(7)
                                                   VALUE 'CCRDSLA'.
          05 LIT-MENUPGM                           PIC X(8)
                                                   VALUE 'COMEN01C'.
          05 LIT-MENUTRANID                        PIC X(4)
                                                   VALUE 'CM00'.
          05 LIT-MENUMAPSET                        PIC X(7)
                                                   VALUE 'COMEN01'.
          05 LIT-MENUMAP                           PIC X(7)
                                                   VALUE 'COMEN1A'.
          05 LIT-CARDDTLPGM                        PIC X(8)
                                                   VALUE 'COCRDSLC'.
          05 LIT-CARDDTLTRANID                     PIC X(4)
                                                   VALUE 'CCDL'.
          05 LIT-CARDDTLMAPSET                     PIC X(7)
                                                   VALUE 'COCRDSL'.
          05 LIT-CARDDTLMAP                        PIC X(7)
                                                   VALUE 'CCRDSLA'.
          05 LIT-ACCTFILENAME                      PIC X(8)
                                                   VALUE 'ACCTDAT '.
          05 LIT-CUSTFILENAME                      PIC X(8)
                                                   VALUE 'CUSTDAT '.
          05 LIT-CARDFILENAME                      PIC X(8)
                                                   VALUE 'CARDDAT '.
          05 LIT-CARDFILENAME-ACCT-PATH            PIC X(8)
                                                   VALUE 'CARDAIX '.
          05 LIT-CARDXREFNAME-ACCT-PATH            PIC X(8)
                                                   VALUE 'CXACAIX '.
      ******************************************************************
      * Literals for use in INSPECT statements
      ******************************************************************
          05 LIT-ALL-ALPHANUM-FROM-X.
             10 LIT-ALL-ALPHA-FROM-X.
                15 LIT-UPPER                       PIC X(26)
                                 VALUE 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.
                15 LIT-LOWER                       PIC X(26)
                                 VALUE 'abcdefghijklmnopqrstuvwxyz'.
             10 LIT-NUMBERS                        PIC X(10)
                                 VALUE '0123456789'.             
      ******************************************************************
      *Other common working storage Variables
      ******************************************************************
       COPY CVCRD01Y.
      ******************************************************************
      *Lookups
      ******************************************************************
