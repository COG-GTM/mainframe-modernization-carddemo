      *****************************************************************
      *    Data-structure for Producto/Articulo (RECLN = 200)
      *    Fichero maestro de productos del catalogo ECI
      *****************************************************************
       01  PRODUCTO-RECORD.
           05  PROD-ID                                 PIC 9(13).
           05  PROD-NOMBRE                             PIC X(60).
           05  PROD-TIPO-CD                            PIC X(02).
           05  PROD-CAT-CD                             PIC 9(04).
           05  PROD-SECCION                            PIC X(30).
           05  PROD-MARCA                              PIC X(30).
           05  PROD-PRECIO-UNITARIO                    PIC S9(07)V99.
           05  PROD-IVA-PCT                            PIC 9(02).
           05  PROD-STOCK-MINIMO                       PIC 9(05).
           05  PROD-ACTIVO                             PIC X(01).
           05  FILLER                                  PIC X(40).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
