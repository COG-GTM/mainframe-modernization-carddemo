package com.carddemo.batch.core;

/** The flat files in {@code app/data/ASCII/} and their copybook record lengths. */
public enum CardDemoFile {
    ACCTDATA("acctdata.txt", 300),
    CARDDATA("carddata.txt", 150),
    CARDXREF("cardxref.txt", 50),
    DAILYTRAN("dailytran.txt", 350),
    DISCGRP("discgrp.txt", 50),
    TCATBAL("tcatbal.txt", 50),
    TRANCATG("trancatg.txt", 60),
    TRANTYPE("trantype.txt", 60);

    private final String fileName;
    private final int recordLength;

    CardDemoFile(String fileName, int recordLength) {
        this.fileName = fileName;
        this.recordLength = recordLength;
    }

    public String fileName() {
        return fileName;
    }

    public int recordLength() {
        return recordLength;
    }
}
