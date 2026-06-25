      *****************************************************************
      *    Data-structure for Tarjeta Fidelizacion ECI (RECLN 150)
      *****************************************************************
       01  TARJETA-RECORD.
           05  TARJ-NUM                           PIC X(16).
           05  TARJ-CLI-ID                        PIC 9(09).
           05  TARJ-TIPO                          PIC X(02).
               88  TARJ-TIPO-ESTANDAR             VALUE 'ST'.
               88  TARJ-TIPO-ORO                  VALUE 'OR'.
               88  TARJ-TIPO-PLATINUM             VALUE 'PL'.
           05  TARJ-NOMBRE-TITULAR                PIC X(50).
           05  TARJ-FECHA-EMISION                 PIC X(10).
           05  TARJ-FECHA-CADUCIDAD               PIC X(10).
           05  TARJ-ACTIVA                        PIC X(01).
               88  TARJ-ACTIVA-SI                 VALUE 'S'.
               88  TARJ-ACTIVA-NO                 VALUE 'N'.
           05  TARJ-CENTRO-EMISION                PIC 9(05).
           05  FILLER                             PIC X(41).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
