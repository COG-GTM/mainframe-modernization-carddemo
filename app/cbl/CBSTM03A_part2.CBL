           05  ST-LINE5.
               10  FILLER  VALUE ALL '-'                PIC X(80).
           05  ST-LINE6.
               10  FILLER  VALUE SPACES                 PIC X(33).
               10  FILLER  VALUE 'Basic Details'        PIC X(14).
               10  FILLER  VALUE SPACES                 PIC X(33).
           05  ST-LINE7.
               10  FILLER  VALUE 'Account ID         :' PIC X(20).
               10  ST-ACCT-ID                           PIC X(20).
               10  FILLER  VALUE SPACES                 PIC X(40).
           05  ST-LINE8.
               10  FILLER  VALUE 'Current Balance    :' PIC X(20).
               10  ST-CURR-BAL                          PIC 9(9).99-.
               10  FILLER  VALUE SPACES                 PIC X(07).
               10  FILLER  VALUE SPACES                 PIC X(40).
           05  ST-LINE9.
               10  FILLER  VALUE 'FICO Score         :' PIC X(20).
               10  ST-FICO-SCORE                        PIC X(20).
               10  FILLER  VALUE SPACES                 PIC X(40).
           05  ST-LINE10.
               10  FILLER  VALUE ALL '-'                PIC X(80).
           05  ST-LINE11.
               10  FILLER  VALUE SPACES                 PIC X(30).
               10  FILLER  VALUE 'TRANSACTION SUMMARY ' PIC X(20).
               10  FILLER  VALUE SPACES                 PIC X(30).
           05  ST-LINE12.
               10  FILLER  VALUE ALL '-'                PIC X(80).
           05  ST-LINE13.
               10  FILLER  VALUE 'Tran ID         '     PIC X(16).
               10  FILLER  VALUE 'Tran Details    '     PIC X(51).
               10  FILLER  VALUE '  Tran Amount'        PIC X(13).
           05  ST-LINE14.
               10  ST-TRANID                            PIC X(16).
               10  FILLER            VALUE ' '          PIC X(01).
               10  ST-TRANDT                            PIC X(49).
               10  FILLER            VALUE '$'          PIC X(01).
               10  ST-TRANAMT                           PIC Z(9).99-.
           05  ST-LINE14A.
               10  FILLER            VALUE 'Total EXP:' PIC X(10).
               10  FILLER            VALUE SPACES       PIC X(56).
               10  FILLER            VALUE '$'          PIC X(01).
               10  ST-TOTAL-TRAMT                       PIC Z(9).99-.
           05  ST-LINE15.
               10  FILLER  VALUE ALL '*'                PIC X(32).
               10  FILLER  VALUE ALL 'END OF STATEMENT' PIC X(16).
               10  FILLER  VALUE ALL '*'                PIC X(32).

       01  HTML-LINES.
           05  HTML-FIXED-LN        PIC X(100).
             88  HTML-L01 VALUE '<!DOCTYPE html>'.
             88  HTML-L02 VALUE '<html lang="en">'.
             88  HTML-L03 VALUE '<head>'.
             88  HTML-L04 VALUE '<meta charset="utf-8">'.
             88  HTML-L05 VALUE '<title>HTML Table Layout</title>'.
             88  HTML-L06 VALUE '</head>'.
             88  HTML-L07 VALUE '<body style="margin:0px;">'.
             88  HTML-L08 VALUE '<table  align="center" frame="box" styl
      -             'e="width:70%; font:12px Segoe UI,sans-serif;">'.
             88  HTML-LTRS VALUE '<tr>'.
             88  HTML-LTRE VALUE '</tr>'.
             88  HTML-LTDS VALUE '<td>'.
             88  HTML-LTDE VALUE '</td>'.
             88  HTML-L10 VALUE '<td colspan="3" style="padding:0px 5px;
      -             'background-color:#1d1d96b3;">'.
             88  HTML-L15 VALUE '<td colspan="3" style="padding:0px 5px;
      -             'background-color:#FFAF33;">'.
             88  HTML-L16
               VALUE '<p style="font-size:16px">Bank of XYZ</p>'.
             88  HTML-L17
               VALUE '<p>410 Terry Ave N</p>'.
             88  HTML-L18
               VALUE '<p>Seattle WA 99999</p>'.
             88  HTML-L22-35
                          VALUE '<td colspan="3" style="padding:0px 5px;
      -              'background-color:#f2f2f2;">'.
             88  HTML-L30-42
                          VALUE '<td colspan="3" style="padding:0px 5px;
      -              'background-color:#33FFD1; text-align:center;">'.
             88  HTML-L31
               VALUE '<p style="font-size:16px">Basic Details</p>'.
             88  HTML-L43
              VALUE '<p style="font-size:16px">Transaction Summary</p>'.
             88  HTML-L47
               VALUE '<td style="width:25%; padding:0px 5px; background-
      -              'color:#33FF5E; text-align:left;">'.
             88  HTML-L48
               VALUE '<p style="font-size:16px">Tran ID</p>'.
             88  HTML-L50
               VALUE '<td style="width:55%; padding:0px 5px; background-
      -              'color:#33FF5E; text-align:left;">'.
             88  HTML-L51
               VALUE '<p style="font-size:16px">Tran Details</p>'.
             88  HTML-L53
               VALUE '<td style="width:20%; padding:0px 5px; background-
      -              'color:#33FF5E; text-align:right;">'.
             88  HTML-L54
               VALUE '<p style="font-size:16px">Amount</p>'.
             88  HTML-L58
               VALUE '<td style="width:25%; padding:0px 5px; background-
      -              'color:#f2f2f2; text-align:left;">'.
