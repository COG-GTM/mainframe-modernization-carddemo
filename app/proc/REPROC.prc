//REPROC  PROC HLQ='MFE.ECIRETAIL'
//*******************************************************************
//* Reprocess tickets procedure
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//SYSIN    DD *
  REPRO INDATASET(&HLQ..TKTBKP(0))                -
        OUTDATASET(&HLQ..TICKETS.VSAM.KSDS)
/*
//         PEND
