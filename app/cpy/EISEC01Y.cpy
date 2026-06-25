      *****************************************************************
      *    Data-structure for User Security record (RECLN = 80)
      *****************************************************************
       01  SEC-USER-RECORD.
           05  SEC-USR-ID                              PIC X(08).
           05  SEC-USR-FNAME                           PIC X(20).
           05  SEC-USR-LNAME                           PIC X(20).
           05  SEC-USR-PWD                             PIC X(08).
           05  SEC-USR-TYPE                            PIC X(01).
               88  SEC-USR-TYPE-ADMIN                  VALUE 'A'.
               88  SEC-USR-TYPE-USER                   VALUE 'U'.
           05  SEC-USR-CENTRO-ID                       PIC 9(05).
           05  FILLER                                  PIC X(18).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
