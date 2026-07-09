package com.carddemo.web.menu.dto;

/**
 * One selectable menu option, mirroring an entry of the option copybooks
 * {@code app/cpy/COMEN02Y.cpy} (main menu) and {@code app/cpy/COADM02Y.cpy} (admin menu).
 *
 * @param number      the option number as displayed ({@code CDEMO-*-OPT-NUM PIC 9(02)},
 *                    two-char zero-padded, e.g. {@code "01"}).
 * @param name        the option label ({@code CDEMO-*-OPT-NAME PIC X(35)}), trimmed.
 * @param tranId      the CICS TRANSID control transfers to when the option is chosen.
 * @param programName the COBOL program ({@code CDEMO-*-OPT-PGMNAME PIC X(08)} / {@code XCTL}
 *                    target).
 * @param adminOnly   whether the option is admin-only ({@code CDEMO-MENU-OPT-USRTYPE = 'A'});
 *                    always {@code true} for admin-menu options.
 */
public record MenuOptionDto(
        String number,
        String name,
        String tranId,
        String programName,
        boolean adminOnly) {
}
