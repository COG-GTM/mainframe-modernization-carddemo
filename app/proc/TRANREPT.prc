//TRANREPT PROC HLQ='MFE.ECIRETAIL'
//*******************************************************************
//* Transaction report procedure
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=EIRPT03C
//STEPLIB  DD DISP=SHR,DSN=&HLQ..LOADLIB
//SYSPRINT DD SYSOUT=*
//VCATBALF DD DISP=SHR,DSN=&HLQ..VCATBALF.VSAM.KSDS
//CNTFILE  DD DISP=SHR,DSN=&HLQ..CENTROS.VSAM.KSDS
//RPTFILE  DD SYSOUT=*
//         PEND
