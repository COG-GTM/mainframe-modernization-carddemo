//DESCTGRP JOB 'LOAD DESCTGRP',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Load Discount/Points policy groups file to VSAM
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//INFILE   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.DESCTGRP.PS
//SYSOUT   DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.DESCTGRP.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                 -
         NAME(MFE.ECIRETAIL.DESCTGRP.VSAM.KSDS)  -
         INDEXED                                  -
         RECSZ(50 50)                             -
         KEYS(16 0)                               -
         RECORDS(500 100)                         -
         FREESPACE(20 10)                         -
         SHAREOPTIONS(2 3) )                      -
         DATA(                                    -
           NAME(MFE.ECIRETAIL.DESCTGRP.VSAM.KSDS.DATA))  -
         INDEX(                                   -
           NAME(MFE.ECIRETAIL.DESCTGRP.VSAM.KSDS.INDEX))
  REPRO INFILE(INFILE)                            -
        OUTDATASET(MFE.ECIRETAIL.DESCTGRP.VSAM.KSDS)
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
