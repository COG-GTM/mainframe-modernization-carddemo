      *****************************************************************
      *    Data-structure for Ticket de Venta record (RECLN = 350)
      *    Transaccion de venta en VSAM master
      *****************************************************************
       01  TICKET-RECORD.
           05  TICKET-ID                               PIC X(16).
           05  TICKET-TIPO-CD                          PIC X(02).
           05  TICKET-CAT-CD                           PIC 9(04).
           05  TICKET-CENTRO-ID                        PIC 9(05).
           05  TICKET-DESC                             PIC X(100).
           05  TICKET-IMPORTE                          PIC S9(09)V99.
           05  TICKET-NUM-ARTICULOS                    PIC 9(03).
           05  TICKET-VENDEDOR-ID                      PIC 9(09).
           05  TICKET-SECCION                          PIC X(30).
           05  TICKET-PLANTA                           PIC X(10).
           05  TICKET-TARJ-NUM                         PIC X(16).
           05  TICKET-FECHA-HORA                       PIC X(26).
           05  TICKET-PROC-TS                          PIC X(26).
           05  TICKET-METODO-PAGO                      PIC X(02).
               88  TICKET-PAGO-TARJETA                 VALUE 'TF'.
               88  TICKET-PAGO-EFECTIVO                VALUE 'EF'.
               88  TICKET-PAGO-MIXTO                   VALUE 'MX'.
           05  FILLER                                  PIC X(88).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
