      *****************************************************************
      *    Data-structure for Cliente ECI entity (RECLN 500)
      *****************************************************************
       01  CLIENTE-RECORD.
           05  CLI-ID                                    PIC 9(09).
           05  CLI-NOMBRE                                PIC X(25).
           05  CLI-APELLIDO1                             PIC X(25).
           05  CLI-APELLIDO2                             PIC X(25).
           05  CLI-DIRECCION-1                           PIC X(50).
           05  CLI-DIRECCION-2                           PIC X(50).
           05  CLI-CIUDAD                                PIC X(30).
           05  CLI-PROVINCIA                             PIC X(30).
           05  CLI-COD-POSTAL                            PIC X(05).
           05  CLI-TELEFONO-1                            PIC X(15).
           05  CLI-TELEFONO-2                            PIC X(15).
           05  CLI-NIF                                   PIC X(09).
           05  CLI-EMAIL                                 PIC X(50).
           05  CLI-FECHA-NACIMIENTO                      PIC X(10).
           05  CLI-FECHA-ALTA                            PIC X(10).
           05  CLI-CENTRO-HABITUAL                       PIC 9(05).
           05  CLI-SEGMENTO                              PIC X(02).
               88  CLI-SEGMENTO-PREMIUM                  VALUE 'PR'.
               88  CLI-SEGMENTO-ESTANDAR                 VALUE 'ST'.
               88  CLI-SEGMENTO-JOVEN                    VALUE 'JV'.
           05  CLI-PUNTOS-ACUMULADOS                     PIC 9(09).
           05  CLI-ACTIVO                                PIC X(01).
           05  FILLER                                    PIC X(125).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
