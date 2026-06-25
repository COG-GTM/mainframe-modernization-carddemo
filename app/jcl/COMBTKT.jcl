//COMBTKT JOB 'COMBINE TICKETS',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Combine system tickets with daily tickets
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=SORT
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SORTIN   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.FIDELTXN(0)
//         DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.DALYTKT.PS
//SORTOUT  DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.DALYTKT.PS
//SYSIN    DD *
  SORT FIELDS=(1,16,CH,A)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
