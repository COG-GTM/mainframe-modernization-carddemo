//DEFGDGB JOB 'DEFINE GDG',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Define GDG Bases for ECIRetail batch outputs
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//SYSIN    DD *
  DEFINE GDG(NAME(MFE.ECIRETAIL.DALYREJS) LIMIT(7) -
         NOEMPTY SCRATCH)
  DEFINE GDG(NAME(MFE.ECIRETAIL.FIDELTXN) LIMIT(7) -
         NOEMPTY SCRATCH)
  DEFINE GDG(NAME(MFE.ECIRETAIL.RPTFILE) LIMIT(7) -
         NOEMPTY SCRATCH)
  DEFINE GDG(NAME(MFE.ECIRETAIL.TRANREPT) LIMIT(7) -
         NOEMPTY SCRATCH)
  DEFINE GDG(NAME(MFE.ECIRETAIL.TKTBKP) LIMIT(7) -
         NOEMPTY SCRATCH)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
