package com.cardemo.migration.model;

/**
 * Describes a VSAM file with its metadata for migration validation.
 *
 * @param name         logical name of the VSAM file (e.g. ACCTDATA)
 * @param fileName     physical file name in the EBCDIC directory
 * @param recordLength fixed record length in bytes
 * @param tableName    corresponding PostgreSQL table name
 * @param copybookName the copybook layout name (e.g. CVACT01Y)
 */
public record VsamFileDescriptor(
        String name,
        String fileName,
        int recordLength,
        String tableName,
        String copybookName
) {

    public static final VsamFileDescriptor ACCTDATA = new VsamFileDescriptor(
            "ACCTDATA", "AWS.M2.CARDDEMO.ACCTDATA.PS", 300,
            "accounts", "CVACT01Y");

    public static final VsamFileDescriptor CARDDATA = new VsamFileDescriptor(
            "CARDDATA", "AWS.M2.CARDDEMO.CARDDATA.PS", 150,
            "cards", "CVACT02Y");

    public static final VsamFileDescriptor CARDXREF = new VsamFileDescriptor(
            "CARDXREF", "AWS.M2.CARDDEMO.CARDXREF.PS", 50,
            "card_xrefs", "CVACT03Y");

    public static final VsamFileDescriptor CUSTDATA = new VsamFileDescriptor(
            "CUSTDATA", "AWS.M2.CARDDEMO.CUSTDATA.PS", 500,
            "customers", "CVCUS01Y");

    public static final VsamFileDescriptor TRANDATA = new VsamFileDescriptor(
            "TRANDATA", "AWS.M2.CARDDEMO.DALYTRAN.PS", 350,
            "transactions", "CVTRA05Y");

    public static final VsamFileDescriptor DALYTRAN = new VsamFileDescriptor(
            "DALYTRAN", "AWS.M2.CARDDEMO.DALYTRAN.PS", 350,
            "daily_transactions", "CVTRA06Y");

    public static final VsamFileDescriptor TCATBALF = new VsamFileDescriptor(
            "TCATBALF", "AWS.M2.CARDDEMO.TCATBALF.PS", 50,
            "tran_cat_balances", "CVTRA01Y");

    public static final VsamFileDescriptor DISCGRP = new VsamFileDescriptor(
            "DISCGRP", "AWS.M2.CARDDEMO.DISCGRP.PS", 50,
            "disclosure_groups", "CVTRA02Y");

    public static final VsamFileDescriptor TRANTYPE = new VsamFileDescriptor(
            "TRANTYPE", "AWS.M2.CARDDEMO.TRANTYPE.PS", 60,
            "transaction_types", "TRANTYPE");

    public static final VsamFileDescriptor TRANCATG = new VsamFileDescriptor(
            "TRANCATG", "AWS.M2.CARDDEMO.TRANCATG.PS", 60,
            "transaction_categories", "TRANCATG");

    public static final VsamFileDescriptor USRSEC = new VsamFileDescriptor(
            "USRSEC", "AWS.M2.CARDDEMO.USRSEC.PS", 80,
            "user_security", "CSUSR01Y");

    public static final VsamFileDescriptor ACCTINDX = new VsamFileDescriptor(
            "ACCTINDX", "AWS.M2.CARDDEMO.ACCDATA.PS", 300,
            "account_index", "CVACT01Y");

    /** All 12 VSAM file descriptors. */
    public static final VsamFileDescriptor[] ALL = {
            ACCTDATA, CARDDATA, CARDXREF, CUSTDATA,
            TRANDATA, DALYTRAN, TCATBALF, DISCGRP,
            TRANTYPE, TRANCATG, USRSEC, ACCTINDX
    };
}
