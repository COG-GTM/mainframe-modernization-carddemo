//FIDCALC JOB 'FIDELITY CALCULATOR',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Process venta category balance file and compute loyalty points
//* and discount amounts per centro.
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP15 EXEC PGM=EIFID04C,PARM='2024011500'
//STEPLIB  DD DISP=SHR,
//            DSN=MFE.ECIRETAIL.LOADLIB
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//VCATBALF DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.VCATBALF.VSAM.KSDS
//XREFFILE DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TARJXREF.VSAM.KSDS
//XREFFIL1 DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TARJXREF.VSAM.AIX.PATH
//CNTFILE  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CENTROS.VSAM.KSDS
//DESCTGRP DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.DESCTGRP.VSAM.KSDS
//FIDELTXN DD DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(RECFM=F,LRECL=350,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=MFE.ECIRETAIL.FIDELTXN(+1)
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
