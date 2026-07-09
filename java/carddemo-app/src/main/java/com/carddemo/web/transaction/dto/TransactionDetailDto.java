package com.carddemo.web.transaction.dto;

import java.math.BigDecimal;

/**
 * The full {@code COTRN01} (Transaction View) map — every display field of the record read
 * in {@code READ-TRANSACT-FILE}, mirroring copybook {@code CVTRA05Y} (TRAN-RECORD).
 *
 * @param tranId        TRNIDO   — TRAN-ID PIC X(16)
 * @param cardNum       CARDNUMO — TRAN-CARD-NUM PIC X(16)
 * @param typeCd        TTYPCDO  — TRAN-TYPE-CD PIC X(02)
 * @param categoryCd    TCATCDO  — TRAN-CAT-CD PIC 9(04)
 * @param source        TRNSRCO  — TRAN-SOURCE PIC X(10)
 * @param amount        TRNAMTO  — TRAN-AMT PIC S9(09)V99
 * @param description   TDESCO   — TRAN-DESC PIC X(100)
 * @param origDate      TORIGDTO — TRAN-ORIG-TS PIC X(26)
 * @param procDate      TPROCDTO — TRAN-PROC-TS PIC X(26)
 * @param merchantId    MIDO     — TRAN-MERCHANT-ID PIC 9(09)
 * @param merchantName  MNAMEO   — TRAN-MERCHANT-NAME PIC X(50)
 * @param merchantCity  MCITYO   — TRAN-MERCHANT-CITY PIC X(50)
 * @param merchantZip   MZIPO    — TRAN-MERCHANT-ZIP PIC X(10)
 */
public record TransactionDetailDto(
        String tranId,
        String cardNum,
        String typeCd,
        Integer categoryCd,
        String source,
        BigDecimal amount,
        String description,
        String origDate,
        String procDate,
        String merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip) {
}
