//CLIENTFL JOB 'LOAD CLIENTES',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load Cliente ECI master file to VSAM KSDS
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.CLIENTES.PS
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.CLIENTES.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                -
         NAME(MFE.ECIRETAIL.CLIENTES.VSAM.KSDS) -
         INDEXED                                 -
         RECSZ(500 500)                          -
         KEYS(9 0)                               -
         RECORDS(10000 2000)                     -
         FREESPACE(20 10)                        -
         SHAREOPTIONS(2 3) )                     -
         DATA(                                   -
           NAME(MFE.ECIRETAIL.CLIENTES.VSAM.KSDS.DATA))  -
         INDEX(                                  -
           NAME(MFE.ECIRETAIL.CLIENTES.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                           -
        OUTDATASET(MFE.ECIRETAIL.CLIENTES.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
