package com.carddemo.online.user;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.SecurityUser;
import com.carddemo.online.common.CommonMessages;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import com.carddemo.repository.SecurityUserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COUSR00C — list of the USRSEC users with paging (transaction CU00), BMS mapset
 * COUSR00, record copybook CSUSR01Y, COMMAREA copybook COCOM01Y plus the CDEMO-CU00-INFO
 * extension carried by {@link UserAdminState}.
 *
 * <p>The CICS browse (STARTBR / READNEXT / READPREV / ENDBR on SEC-USR-ID) becomes keyset paging
 * on the primary key: a page forward reads the ten users at or after the browse key plus one more
 * to set CDEMO-CU00-NEXT-PAGE-FLG, a page backward reads the ten users before the first user of
 * the page.
 */
@Service
public class UserListService {

    /** The screen holds ten lines (USER-REC OCCURS 10 TIMES). */
    private static final int PAGE_SIZE = 10;

    /** RIDFLD set to LOW-VALUES: browse from the start of the file. */
    private static final String LOW_VALUES = "";

    /** RIDFLD set to HIGH-VALUES: browse positioned past the last record. */
    private static final String HIGH_VALUES = "\uffff";

    private final SecurityUserRepository securityUserRepository;

    public UserListService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /**
     * First entry with a COMMAREA but {@code NOT CDEMO-PGM-REENTER}: COUSR00C performs
     * PROCESS-ENTER-KEY with an empty map, which lists the first page.
     */
    public UserListResponse initialScreen(CardDemoCommarea commarea, UserAdminState state) {
        commarea.setProgramContext(1);
        return processEnterKey(new UserListRequest(null, List.of()), commarea, state);
    }

    /** COBOL paragraph: PROCESS-ENTER-KEY. */
    public UserListResponse processEnterKey(
            UserListRequest request, CardDemoCommarea commarea, UserAdminState state) {
        UserRow selected = firstSelectedRow(request.rowsOrEmpty());
        state.setSelectionFlag(selected == null ? null : selected.selection());
        state.setSelectedUserId(selected == null ? null : selected.userId());

        String message = null;
        if (selected != null && isNotBlank(selected.selection()) && isNotBlank(selected.userId())) {
            String flag = selected.selection().trim();
            if ("U".equalsIgnoreCase(flag)) {
                return transferTo(OnlinePrograms.USER_UPDATE, commarea, state, request.rowsOrEmpty());
            }
            if ("D".equalsIgnoreCase(flag)) {
                return transferTo(OnlinePrograms.USER_DELETE, commarea, state, request.rowsOrEmpty());
            }
            // The browse still runs after an unusable selection: only the message is kept.
            message = "Invalid selection. Valid values are U and D";
        }

        String browseKey = isNotBlank(request.userId()) ? request.userId().trim() : LOW_VALUES;
        state.setPageNumber(0);
        return pageForward(browseKey, false, state, message);
    }

    /** COBOL paragraph: PROCESS-PF7-KEY (page backward). */
    public UserListResponse processPf7Key(UserListRequest request, UserAdminState state) {
        String browseKey = isNotBlank(state.getFirstUserId()) ? state.getFirstUserId() : LOW_VALUES;
        state.setNextPage(true);

        if (state.getPageNumber() > 1) {
            return pageBackward(browseKey, state);
        }
        return screen(request.rowsOrEmpty(), state, "You are already at the top of the page...");
    }

    /** COBOL paragraph: PROCESS-PF8-KEY (page forward). */
    public UserListResponse processPf8Key(UserListRequest request, UserAdminState state) {
        String browseKey = isNotBlank(state.getLastUserId()) ? state.getLastUserId() : HIGH_VALUES;

        if (state.isNextPage()) {
            return pageForward(browseKey, true, state, null);
        }
        return screen(request.rowsOrEmpty(), state, "You are already at the bottom of the page...");
    }

    /** DFHPF3: back to the admin menu. */
    public UserListResponse processPf3Key(CardDemoCommarea commarea, UserAdminState state) {
        return returnToPreviousScreen(OnlinePrograms.ADMIN_MENU, commarea, state);
    }

    /** {@code WHEN OTHER} of the EVALUATE EIBAID. */
    public UserListResponse processOtherKey(UserListRequest request, UserAdminState state) {
        return screen(request.rowsOrEmpty(), state, CommonMessages.INVALID_KEY);
    }

    /** COBOL paragraph: PROCESS-PAGE-FORWARD (STARTBR + up to ten READNEXT + one look ahead). */
    private UserListResponse pageForward(
            String browseKey, boolean skipPositionedRecord, UserAdminState state, String message) {
        if (startBrowseFails(browseKey)) {
            // STARTBR answered NOTFND.
            return screen(List.of(), state, message != null ? message : "You are at the top of the page...");
        }

        PageRequest window = PageRequest.of(0, PAGE_SIZE + 1);
        List<SecurityUser> browsed = skipPositionedRecord
                ? securityUserRepository.findByUserIdGreaterThanOrderByUserIdAsc(browseKey, window)
                : securityUserRepository.findByUserIdGreaterThanEqualOrderByUserIdAsc(browseKey, window);

        List<SecurityUser> page = browsed.stream().limit(PAGE_SIZE).toList();
        String pageMessage = message;

        if (page.size() < PAGE_SIZE) {
            // READNEXT answered ENDFILE while the page was being filled.
            pageMessage = pageMessage != null ? pageMessage : "You have reached the bottom of the page...";
            state.setNextPage(false);
            if (!page.isEmpty()) {
                state.setPageNumber(state.getPageNumber() + 1);
            }
        } else {
            state.setPageNumber(state.getPageNumber() + 1);
            boolean hasFollowingRecord = browsed.size() > PAGE_SIZE;
            state.setNextPage(hasFollowingRecord);
            if (!hasFollowingRecord) {
                pageMessage = pageMessage != null ? pageMessage : "You have reached the bottom of the page...";
            }
        }

        if (!page.isEmpty()) {
            state.setFirstUserId(page.get(0).getUserId());
        }
        if (page.size() == PAGE_SIZE) {
            state.setLastUserId(page.get(PAGE_SIZE - 1).getUserId());
        }
        return screen(toRows(page), state, pageMessage);
    }

    /** COBOL paragraph: PROCESS-PAGE-BACKWARD (STARTBR + up to ten READPREV + one look back). */
    private UserListResponse pageBackward(String browseKey, UserAdminState state) {
        PageRequest window = PageRequest.of(0, PAGE_SIZE + 1);
        List<SecurityUser> browsed =
                securityUserRepository.findByUserIdLessThanOrderByUserIdDesc(browseKey, window);

        List<SecurityUser> page = new ArrayList<>(browsed.stream().limit(PAGE_SIZE).toList());
        Collections.reverse(page);
        String message = null;

        if (page.size() < PAGE_SIZE) {
            // READPREV answered ENDFILE while the page was being filled; CDEMO-CU00-PAGE-NUM is
            // then left untouched by COUSR00C.
            message = "You have reached the top of the page...";
        } else if (browsed.size() > PAGE_SIZE && state.getPageNumber() > 1) {
            state.setPageNumber(state.getPageNumber() - 1);
        } else {
            if (browsed.size() == PAGE_SIZE) {
                message = "You have reached the top of the page...";
            }
            state.setPageNumber(1);
        }

        if (!page.isEmpty()) {
            state.setLastUserId(page.get(page.size() - 1).getUserId());
        }
        if (page.size() == PAGE_SIZE) {
            state.setFirstUserId(page.get(0).getUserId());
        }
        return screen(toRows(page), state, message);
    }

    /** COBOL paragraph: RETURN-TO-PREV-SCREEN. */
    private UserListResponse returnToPreviousScreen(
            String toProgram, CardDemoCommarea commarea, UserAdminState state) {
        commarea.setToProgram(toProgram);
        commarea.setToTransactionId(OnlinePrograms.transactionIdOf(toProgram));
        commarea.setFromTransactionId(OnlinePrograms.TRANID_USER_LIST);
        commarea.setFromProgram(OnlinePrograms.USER_LIST);
        commarea.setProgramContext(0);
        return new UserListResponse(
                header(),
                List.of(),
                state.getPageNumber(),
                state.isNextPage(),
                null,
                toProgram,
                OnlinePrograms.transactionIdOf(toProgram));
    }

    /** The XCTL performed for the 'U' and 'D' selections. */
    private UserListResponse transferTo(
            String toProgram, CardDemoCommarea commarea, UserAdminState state, List<UserRow> rows) {
        commarea.setToProgram(toProgram);
        commarea.setToTransactionId(OnlinePrograms.transactionIdOf(toProgram));
        commarea.setFromTransactionId(OnlinePrograms.TRANID_USER_LIST);
        commarea.setFromProgram(OnlinePrograms.USER_LIST);
        commarea.setProgramContext(0);
        return new UserListResponse(
                header(),
                rows,
                state.getPageNumber(),
                state.isNextPage(),
                null,
                toProgram,
                OnlinePrograms.transactionIdOf(toProgram));
    }

    private boolean startBrowseFails(String browseKey) {
        return securityUserRepository
                .findByUserIdGreaterThanEqualOrderByUserIdAsc(browseKey, PageRequest.of(0, 1))
                .isEmpty();
    }

    /** The EVALUATE over SEL0001I .. SEL0010I: the first non blank line wins. */
    private static UserRow firstSelectedRow(List<UserRow> rows) {
        return rows.stream().filter(row -> isNotBlank(row.selection())).findFirst().orElse(null);
    }

    private static List<UserRow> toRows(List<SecurityUser> users) {
        return users.stream()
                .map(user -> new UserRow(
                        null,
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserType()))
                .toList();
    }

    private UserListResponse screen(List<UserRow> rows, UserAdminState state, String message) {
        return new UserListResponse(
                header(), rows, state.getPageNumber(), state.isNextPage(), message, null, null);
    }

    private ScreenHeader header() {
        return ScreenHeader.of(OnlinePrograms.TRANID_USER_LIST, OnlinePrograms.USER_LIST);
    }

    /** {@code NOT = SPACES AND LOW-VALUES}. */
    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
