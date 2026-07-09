package com.carddemo.web.menu.dto;

import java.util.List;

/**
 * The rendered menu returned by {@code GET /api/menu} and {@code GET /api/menu/admin} — the
 * REST analogue of the BMS map {@code COMEN01C}/{@code COADM01C} builds via
 * {@code BUILD-MENU-OPTIONS}, carrying the option list filtered for the signed-on user.
 *
 * @param menu        logical menu identifier ({@code MAIN_MENU} / {@code ADMIN_MENU}).
 * @param tranId      the menu's CICS TRANSID ({@code CM00} / {@code CA00}).
 * @param programName the menu COBOL program ({@code COMEN01C} / {@code COADM01C}).
 * @param title       human-readable menu title.
 * @param userId      the signed-on user id (from the authenticated principal).
 * @param userType    the signed-on user type ({@code USER} / {@code ADMIN}).
 * @param options     the options visible to this user.
 */
public record MenuResponse(
        String menu,
        String tranId,
        String programName,
        String title,
        String userId,
        String userType,
        List<MenuOptionDto> options) {
}
