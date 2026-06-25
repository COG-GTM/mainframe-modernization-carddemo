//DUSRSECJ JOB 'USER SECURITY',CLASS=A,MSGCLASS=0,
//   NOTIFY=&SYSUID
//*******************************************************************
//* Setup user security VSAM file
//* ECIRetail - El Corte Ingles Retail System
//*******************************************************************
//STEP01 EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//SYSIN    DD *
  DELETE MFE.ECIRETAIL.USRSEC.VSAM.KSDS -
         CLUSTER PURGE
  SET MAXCC = 0
  DEFINE CLUSTER(                                -
         NAME(MFE.ECIRETAIL.USRSEC.VSAM.KSDS)   -
         INDEXED                                 -
         RECSZ(80 80)                            -
         KEYS(8 0)                               -
         RECORDS(100 20)                         -
         FREESPACE(20 10)                        -
         SHAREOPTIONS(2 3) )                     -
         DATA(                                   -
           NAME(MFE.ECIRETAIL.USRSEC.VSAM.KSDS.DATA))  -
         INDEX(                                  -
           NAME(MFE.ECIRETAIL.USRSEC.VSAM.KSDS.INDEX))
/*
//STEP02 EXEC PGM=IEBGENER
//SYSPRINT DD SYSOUT=*
//SYSIN    DD DUMMY
//SYSUT2   DD DISP=SHR,
//         DSN=MFE.ECIRETAIL.USRSEC.VSAM.KSDS
//SYSUT1   DD *
ADMIN001ADMIN               SISTEMAS        PASSWORDA00001
USER0001CARLOS              VENTAS          PASSWORDU00042
USER0002MARIA               ATENCION CLI    PASSWORDU00001
/*
//*
//* Ver: ECIRetail_v1.0 Date: 2024-01-15
//*
