//OPENFIL JOB 'OPEN FILES',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Open/Make files available to CICS
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IEFBR14
//CNTROFL  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CENTROS.VSAM.KSDS
//CLIENTFL DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CLIENTES.VSAM.KSDS
//TARJFLFL DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TARJETAS.VSAM.KSDS
//TICKTFL  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TICKETS.VSAM.KSDS
//XREFFILE DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TARJXREF.VSAM.KSDS
//VCATBALF DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.VCATBALF.VSAM.KSDS
//USRSECFL DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.USRSEC.VSAM.KSDS
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
