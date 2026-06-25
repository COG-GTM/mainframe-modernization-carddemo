//CREASTMT JOB 'SALES STATEMENT',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Produce daily sales statement per centro
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=EISTM03A,PARM='2024-01-15'
//STEPLIB  DD DISP=SHR,
//            DSN=MFE.ECIRETAIL.LOADLIB
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//TKTFILE  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TICKETS.VSAM.KSDS
//CNTFILE  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CENTROS.VSAM.KSDS
//RPTFILE  DD DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(RECFM=F,LRECL=132,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=MFE.ECIRETAIL.RPTFILE(+1)
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
