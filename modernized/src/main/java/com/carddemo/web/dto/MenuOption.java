package com.carddemo.web.dto;

/** One entry of the COMEN02Y / COADM02Y menu option tables. */
public record MenuOption(int option, String name, String legacyProgram, String endpoint) {
}
