      *****************************************************************
      *    Data-structure for Categoria de producto (RECLN = 60)
      *    Equivalente a TRANCATG
      *****************************************************************
       01  PROD-CAT-RECORD.
           05  PROD-CAT-KEY.
              10  PROD-TIPO-CD                         PIC X(02).
              10  PROD-CAT-CD                          PIC 9(04).
           05  PROD-CAT-DESC                           PIC X(50).
           05  FILLER                                  PIC X(04).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
