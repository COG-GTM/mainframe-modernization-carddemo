*****************************************************************
* Control file for batch reprocessing schedule
* ECIRetail - El Corte Ingles Retail System
*****************************************************************
* CENTRO-RANGE: first and last centro to process
* PROCESS-DATE: date to reprocess (YYYY-MM-DD)
* MODE: FULL (all centros) or RANGE (specified range)
*****************************************************************
MODE=FULL
CENTRO-RANGE=00001,00123
PROCESS-DATE=2024-01-15
MAX-REJECTS=100
NOTIFY-ON-FAIL=SISTEMAS@ECI.ES
