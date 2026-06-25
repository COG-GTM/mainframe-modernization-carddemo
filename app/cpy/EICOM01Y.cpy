      *****************************************************************
      *    COMMAREA structure for ECIRETAIL online programs
      *****************************************************************
       01  CARDDEMO-COMMAREA.
           05  CDEMO-FROM-TRANID                PIC X(04).
           05  CDEMO-FROM-PROGRAM               PIC X(08).
           05  CDEMO-TO-TRANID                  PIC X(04).
           05  CDEMO-TO-PROGRAM                 PIC X(08).
           05  CDEMO-USER-ID                    PIC X(08).
           05  CDEMO-USER-TYPE                  PIC X(01).
           05  CDEMO-USER-CENTRO-ID             PIC 9(05).
           05  CDEMO-PGM-CONTEXT                PIC 9(01).
           05  CDEMO-LAST-MAP                   PIC X(07).
           05  CDEMO-LAST-MAPSET                PIC X(07).
           05  FILLER                           PIC X(447).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
