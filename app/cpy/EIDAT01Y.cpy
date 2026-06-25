      *****************************************************************
      *    Date and time handling copybook
      *****************************************************************
       01  WS-CURDATE-DATA.
           05  WS-CURDATE.
               10  WS-CURDATE-YEAR             PIC 9(04).
               10  WS-CURDATE-MONTH            PIC 9(02).
               10  WS-CURDATE-DAY              PIC 9(02).
           05  WS-CURTIME.
               10  WS-CURTIME-HOURS            PIC 9(02).
               10  WS-CURTIME-MINUTE           PIC 9(02).
               10  WS-CURTIME-SECOND           PIC 9(02).
               10  WS-CURTIME-MILSEC           PIC 9(02).
           05  WS-CURDATE-FORMATTED.
               10  WS-CURDATE-FMT-YEAR         PIC X(04).
               10  FILLER                      PIC X VALUE '-'.
               10  WS-CURDATE-FMT-MONTH        PIC X(02).
               10  FILLER                      PIC X VALUE '-'.
               10  WS-CURDATE-FMT-DAY          PIC X(02).
      *
      * Ver: ECIRetail_v1.0 Date: 2024-01-15
      *
