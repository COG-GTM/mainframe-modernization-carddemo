//VCATBALF JOB 'LOAD VCATBAL',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load initial venta category balance file to VSAM
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.VCATBAL.PS
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.VCATBALF.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                 -
         NAME(MFE.ECIRETAIL.VCATBALF.VSAM.KSDS)  -
         INDEXED                                  -
         RECSZ(50 50)                             -
         KEYS(11 0)                               -
         RECORDS(5000 1000)                       -
         FREESPACE(20 10)                         -
         SHAREOPTIONS(2 3) )                      -
         DATA(                                    -
           NAME(MFE.ECIRETAIL.VCATBALF.VSAM.KSDS.DATA))  -
         INDEX(                                   -
           NAME(MFE.ECIRETAIL.VCATBALF.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                            -
        OUTDATASET(MFE.ECIRETAIL.VCATBALF.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
