      *****************************************************************
      *    Data-structure for Saldo ventas por categoria/centro
      *    (RECLN = 50) - Equivalente a TCATBALF
      *****************************************************************
       01  VENTA-CAT-BAL-RECORD.
           05  VENTA-CAT-KEY.
              10 VCATBAL-CENTRO-ID                     PIC 9(05).
              10 VCATBAL-TIPO-CD                       PIC X(02).
              10 VCATBAL-CAT-CD                        PIC 9(04).
           05  VENTA-CAT-BAL                           PIC S9(09)V99.
           05  VENTA-CAT-PUNTOS                        PIC 9(07).
           05  FILLER                                  PIC X(10).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
