//TKTBKP  JOB 'TICKET BACKUP',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Backup Ticket master database (VSAM to GDG)
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  REPRO INDATASET(MFE.ECIRETAIL.TICKETS.VSAM.KSDS)  -
        OUTDATASET(MFE.ECIRETAIL.TKTBKP(+1))
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
