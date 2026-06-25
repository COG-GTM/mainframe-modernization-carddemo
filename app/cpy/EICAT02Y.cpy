      *****************************************************************
      *    Data-structure for Politica descuentos/puntos (RECLN = 50)
      *    Equivalente a DISCGRP (disclosure group)
      *    Define puntos y descuento por tipo/cat de producto y segmento
      *****************************************************************
       01  DESCTO-RECORD.
           05  DESCTO-KEY.
              10 DESCTO-SEGMENTO-ID                    PIC X(10).
              10 DESCTO-TIPO-CD                        PIC X(02).
              10 DESCTO-CAT-CD                         PIC 9(04).
           05  DESCTO-PCT-DESCUENTO                    PIC S9(02)V99.
           05  DESCTO-PUNTOS-X-EURO                    PIC 9(03).
           05  FILLER                                  PIC X(25).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
