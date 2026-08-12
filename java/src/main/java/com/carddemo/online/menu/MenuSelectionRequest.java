package com.carddemo.online.menu;

/**
 * BMS input field OPTIONI of maps COMEN1A / COADM1A (COBOL programs COMEN01C and COADM01C).
 *
 * @param option OPTIONI PIC X(02), exactly as keyed by the operator
 */
public record MenuSelectionRequest(String option) {
}
