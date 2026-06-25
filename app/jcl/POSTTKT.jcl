//POSTTKT JOB 'POST TICKETS',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Process and load daily ticket file, update ticket master VSAM
//* and venta category balance.
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP15 EXEC PGM=EITRN02C
//STEPLIB  DD DISP=SHR,
//            DSN=MFE.ECIRETAIL.LOADLIB
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//TKTFILE  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TICKETS.VSAM.KSDS
//DALYTKT  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.DALYTKT.PS
//XREFFILE DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TARJXREF.VSAM.KSDS
//DALYREJS DD DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(RECFM=F,LRECL=430,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=MFE.ECIRETAIL.DALYREJS(+1)
//CNTFILE  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CENTROS.VSAM.KSDS
//VCATBALF DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.VCATBALF.VSAM.KSDS
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
