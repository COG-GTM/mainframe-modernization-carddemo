package com.carddemo.online.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: CORPT00C — input fields of BMS map CORPT0A (mapset CORPT00).
 *
 * <p>MONTHLY, YEARLY and CUSTOM are the three (mutually exclusive on the screen, but evaluated in
 * that order by the program) selection fields; the six date fields only matter for a custom range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportRequest {

    /** MONTHLY PIC X(01) — any non blank value selects the monthly report. */
    private String monthly;

    /** YEARLY PIC X(01). */
    private String yearly;

    /** CUSTOM PIC X(01). */
    private String custom;

    /** SDTMM PIC X(02). */
    private String startMonth;

    /** SDTDD PIC X(02). */
    private String startDay;

    /** SDTYYYY PIC X(04). */
    private String startYear;

    /** EDTMM PIC X(02). */
    private String endMonth;

    /** EDTDD PIC X(02). */
    private String endDay;

    /** EDTYYYY PIC X(04). */
    private String endYear;

    /** CONFIRM PIC X(01): 'Y' submits the job, 'N' clears the screen, blank re-prompts. */
    private String confirm;
}
