//PRODTYPE JOB 'LOAD PRODTYPE',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load product type file to VSAM
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.PRODTYPE.PS
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.PRODTYPE.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                 -
         NAME(MFE.ECIRETAIL.PRODTYPE.VSAM.KSDS)  -
         INDEXED                                  -
         RECSZ(60 60)                             -
         KEYS(2 0)                                -
         RECORDS(50 10)                           -
         FREESPACE(20 10)                         -
         SHAREOPTIONS(2 3) )                      -
         DATA(                                    -
           NAME(MFE.ECIRETAIL.PRODTYPE.VSAM.KSDS.DATA))  -
         INDEX(                                   -
           NAME(MFE.ECIRETAIL.PRODTYPE.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                            -
        OUTDATASET(MFE.ECIRETAIL.PRODTYPE.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
