//CNTROFL JOB 'LOAD CENTROS',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load Centro/Tienda master file to VSAM KSDS
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CENTROS.PS
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.CENTROS.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                              -
         NAME(MFE.ECIRETAIL.CENTROS.VSAM.KSDS) -
         INDEXED                               -
         RECSZ(300 300)                        -
         KEYS(5 0)                             -
         RECORDS(200 50)                       -
         FREESPACE(20 10)                      -
         SHAREOPTIONS(2 3) )                   -
         DATA(                                 -
           NAME(MFE.ECIRETAIL.CENTROS.VSAM.KSDS.DATA))  -
         INDEX(                                -
           NAME(MFE.ECIRETAIL.CENTROS.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                         -
        OUTDATASET(MFE.ECIRETAIL.CENTROS.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
