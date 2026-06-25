      *****************************************************************
      *    Data-structure for Ticket Diario record (RECLN = 350)
      *    Tickets de venta del dia pendientes de posteo
      *****************************************************************
       01  DALYTICKET-RECORD.
           05  DALYTICKET-ID                           PIC X(16).
           05  DALYTICKET-TIPO-CD                      PIC X(02).
           05  DALYTICKET-CAT-CD                       PIC 9(04).
           05  DALYTICKET-CENTRO-ID                    PIC 9(05).
           05  DALYTICKET-DESC                         PIC X(100).
           05  DALYTICKET-IMPORTE                      PIC S9(09)V99.
           05  DALYTICKET-NUM-ARTICULOS                PIC 9(03).
           05  DALYTICKET-VENDEDOR-ID                  PIC 9(09).
           05  DALYTICKET-SECCION                      PIC X(30).
           05  DALYTICKET-PLANTA                       PIC X(10).
           05  DALYTICKET-TARJ-NUM                     PIC X(16).
           05  DALYTICKET-FECHA-HORA                   PIC X(26).
           05  DALYTICKET-PROC-TS                      PIC X(26).
           05  DALYTICKET-METODO-PAGO                  PIC X(02).
           05  FILLER                                  PIC X(88).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
