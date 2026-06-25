//TKTAIDX JOB 'TICKET AIX',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Define alternate index on ticket file (by centro)
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//SYSIN    DD *
  DEFINE AIX(                                          -
         NAME(MFE.ECIRETAIL.TICKETS.VSAM.AIX)         -
         RELATE(MFE.ECIRETAIL.TICKETS.VSAM.KSDS)      -
         KEYS(5 22)                                    -
         NONUNIQUEKEY                                  -
         UPGRADE                                       -
         RECORDSIZE(350 350)                           -
         SHAREOPTIONS(2 3) )
  DEFINE PATH(                                         -
         NAME(MFE.ECIRETAIL.TICKETS.VSAM.AIX.PATH)    -
         PATHENTRY(MFE.ECIRETAIL.TICKETS.VSAM.AIX) )
  BLDINDEX INDATASET(MFE.ECIRETAIL.TICKETS.VSAM.KSDS) -
           OUTDATASET(MFE.ECIRETAIL.TICKETS.VSAM.AIX)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
