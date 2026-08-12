package com.carddemo.online.user;

import java.util.List;

/**
 * BMS map COUSR0A (mapset COUSR00) input of COBOL program COUSR00C.
 *
 * @param userId USRIDINI PIC X(08), the key the browse is repositioned on
 * @param rows the ten screen lines in order; PROCESS-ENTER-KEY keeps the first one whose
 *     selection field is not blank (SEL0001I .. SEL0010I)
 */
public record UserListRequest(String userId, List<UserRow> rows) {

    public List<UserRow> rowsOrEmpty() {
        return rows == null ? List.of() : rows;
    }
}
