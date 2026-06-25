      *****************************************************************
      *    Data-structure for Centro/Tienda entity (RECLN 300)
      *    Maestro de los 123 centros de El Corte Ingles
      *****************************************************************
       01  CENTRO-RECORD.
           05  CENTRO-ID                          PIC 9(05).
           05  CENTRO-NOMBRE                      PIC X(50).
           05  CENTRO-DIRECCION                   PIC X(80).
           05  CENTRO-CIUDAD                      PIC X(30).
           05  CENTRO-PROVINCIA                   PIC X(30).
           05  CENTRO-COD-POSTAL                  PIC X(05).
           05  CENTRO-TELEFONO                    PIC X(15).
           05  CENTRO-TIPO                        PIC X(02).
               88  CENTRO-TIPO-GRANDE             VALUE 'GR'.
               88  CENTRO-TIPO-MEDIANO            VALUE 'MD'.
               88  CENTRO-TIPO-OUTLET             VALUE 'OT'.
               88  CENTRO-TIPO-HIPERCOR           VALUE 'HC'.
           05  CENTRO-SUPERFICIE-M2               PIC 9(06).
           05  CENTRO-FECHA-APERTURA              PIC X(10).
           05  CENTRO-ACTIVO                      PIC X(01).
               88  CENTRO-ACTIVO-SI               VALUE 'S'.
               88  CENTRO-ACTIVO-NO               VALUE 'N'.
           05  CENTRO-NUM-EMPLEADOS               PIC 9(05).
           05  CENTRO-ZONA-COMERCIAL              PIC X(10).
           05  FILLER                             PIC X(51).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
