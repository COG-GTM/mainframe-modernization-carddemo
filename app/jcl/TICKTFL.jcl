//TICKTFL JOB 'LOAD TICKETS',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load initial Ticket master file to VSAM KSDS
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.TICKETS.PS.INIT
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.TICKETS.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                 -
         NAME(MFE.ECIRETAIL.TICKETS.VSAM.KSDS)   -
         INDEXED                                  -
         RECSZ(350 350)                           -
         KEYS(16 0)                               -
         RECORDS(100000 10000)                    -
         FREESPACE(20 10)                         -
         SHAREOPTIONS(2 3) )                      -
         DATA(                                    -
           NAME(MFE.ECIRETAIL.TICKETS.VSAM.KSDS.DATA))  -
         INDEX(                                   -
           NAME(MFE.ECIRETAIL.TICKETS.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                            -
        OUTDATASET(MFE.ECIRETAIL.TICKETS.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
