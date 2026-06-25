//PRODCATG JOB 'LOAD PRODCATG',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load product category file to VSAM
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.PRODCATG.PS
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.PRODCATG.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                 -
         NAME(MFE.ECIRETAIL.PRODCATG.VSAM.KSDS)  -
         INDEXED                                  -
         RECSZ(60 60)                             -
         KEYS(6 0)                                -
         RECORDS(200 50)                          -
         FREESPACE(20 10)                         -
         SHAREOPTIONS(2 3) )                      -
         DATA(                                    -
           NAME(MFE.ECIRETAIL.PRODCATG.VSAM.KSDS.DATA))  -
         INDEX(                                   -
           NAME(MFE.ECIRETAIL.PRODCATG.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                            -
        OUTDATASET(MFE.ECIRETAIL.PRODCATG.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
