//TESTUSRJ JOB 'TEST USRSEC FILE',REGION=8M,CLASS=A,
//      MSGCLASS=H,NOTIFY=&SYSUID
//******************************************************************
//* Test User Security File Setup for CC00 Login Testing
//* Based on DUSRSECJ.jcl with additional test users
//******************************************************************
//*-------------------------------------------------------------------*
//* PRE DELETE STEP
//*-------------------------------------------------------------------*
//*
//PREDEL  EXEC PGM=IEFBR14
//*
//DD01     DD DSN=TEST.CARDDEMO.USRSEC.PS,
//            DISP=(MOD,DELETE,DELETE)
//*
//*-------------------------------------------------------------------*
//* CREATE TEST USER SECURITY FILE (PS) FROM IN-STREAM DATA
//*-------------------------------------------------------------------*
//*
//STEP01  EXEC PGM=IEBGENER
//*
//SYSUT1   DD *
ADMIN001MARGARET            GOLD                PASSWORDA
USER0001LAWRENCE            THOMAS              PASSWORDU
INVALID01TEST               USER                PASSWORDU
TESTUSER TEST               USER                TESTPASSU
EDGECASE EDGE               CASE                12345678U
/*
//SYSUT2   DD DSN=TEST.CARDDEMO.USRSEC.PS,
//            DISP=(NEW,CATLG,DELETE),
//            DCB=(LRECL=80,RECFM=FB,DSORG=PS,BLKSIZE=0),
//            UNIT=SYSDA,SPACE=(TRK,(10,5),RLSE)
//*
//SYSPRINT DD SYSOUT=*
//SYSIN    DD DUMMY
//*
//*-------------------------------------------------------------------*
//* DEFINE VSAM FILE FOR TEST USER SECURITY
//*-------------------------------------------------------------------*
//*
//STEP02  EXEC PGM=IDCAMS
//*
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
 DELETE                  TEST.CARDDEMO.USRSEC.VSAM.KSDS
 SET       MAXCC = 0
 DEFINE    CLUSTER (NAME(TEST.CARDDEMO.USRSEC.VSAM.KSDS)    -
                    KEYS(8,0)                                 -
                    RECORDSIZE(80,80)                         -
                    REUSE                                     -
                    INDEXED                                   -
                    TRACKS(45,15)                             -
                    FREESPACE(10,15)                          -
                    CISZ(8192))                               -
           DATA    (NAME(TEST.CARDDEMO.USRSEC.VSAM.KSDS.DAT)) -
           INDEX   (NAME(TEST.CARDDEMO.USRSEC.VSAM.KSDS.IDX))
/*
//*
//*-------------------------------------------------------------------*
//* COPY TEST USER SECURITY DATA FROM PS TO VSAM FILE
//*-------------------------------------------------------------------*
//*
//STEP03  EXEC PGM=IDCAMS
//*
//IN       DD  DSN=TEST.CARDDEMO.USRSEC.PS,DISP=SHR
//OUT      DD  DSN=TEST.CARDDEMO.USRSEC.VSAM.KSDS,DISP=SHR
//SYSOUT   DD  SYSOUT=*
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
  REPRO INFILE(IN) OUTFILE(OUT)
/*
//
