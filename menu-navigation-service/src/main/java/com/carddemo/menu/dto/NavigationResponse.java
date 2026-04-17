package com.carddemo.menu.dto;

/**
 * Response for POST /api/v1/menu/navigate.
 * Contains the target program and navigation context, mirroring
 * the COMMAREA fields set during XCTL in COMEN01C.cbl.
 */
public record NavigationResponse(
        String sessionId,
        String fromProgram,
        String toProgram,
        String targetProgramName,
        String optionName,
        int pgmContext
) {
}
