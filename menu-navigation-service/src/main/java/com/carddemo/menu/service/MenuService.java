package com.carddemo.menu.service;

import com.carddemo.menu.dto.MenuItemResponse;
import com.carddemo.menu.dto.MenuResponse;
import com.carddemo.menu.dto.NavigationContextResponse;
import com.carddemo.menu.dto.NavigationRequest;
import com.carddemo.menu.dto.NavigationResponse;
import com.carddemo.menu.entity.MenuGroup;
import com.carddemo.menu.entity.MenuItem;
import com.carddemo.menu.entity.NavigationContext;
import com.carddemo.menu.entity.UserType;
import com.carddemo.menu.exception.InvalidMenuOptionException;
import com.carddemo.menu.exception.NavigationContextNotFoundException;
import com.carddemo.menu.exception.UnauthorizedMenuAccessException;
import com.carddemo.menu.repository.MenuItemRepository;
import com.carddemo.menu.repository.NavigationContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing the menu navigation business logic ported from
 * COMEN01C.cbl (regular user menu) and COADM01C.cbl (admin menu).
 *
 * Key COBOL paragraphs ported:
 * - BUILD-MENU-OPTIONS: iterates CDEMO-MENU-OPT-COUNT, filtered by user type
 * - PROCESS-ENTER-KEY: validates option, checks user type access, routes via XCTL
 * - RETURN-TO-SIGNON-SCREEN: PF3 back-navigation
 */
@Service
public class MenuService {

    private static final String REGULAR_MENU_PROGRAM = "COMEN01C";
    private static final String ADMIN_MENU_PROGRAM = "COADM01C";
    private static final String REGULAR_MENU_TRANID = "CM00";
    private static final String ADMIN_MENU_TRANID = "CA00";

    private final MenuItemRepository menuItemRepository;
    private final NavigationContextRepository navigationContextRepository;

    public MenuService(MenuItemRepository menuItemRepository,
                       NavigationContextRepository navigationContextRepository) {
        this.menuItemRepository = menuItemRepository;
        this.navigationContextRepository = navigationContextRepository;
    }

    /**
     * Returns menu items filtered by user role.
     * Admin users get the admin menu (COADM02Y.cpy options).
     * Regular users get the regular menu (COMEN02Y.cpy options).
     *
     * This mirrors the BUILD-MENU-OPTIONS paragraph where options are
     * iterated from 1 to CDEMO-MENU-OPT-COUNT and filtered by CDEMO-MENU-OPT-USRTYPE.
     */
    @Transactional(readOnly = true)
    public MenuResponse getMenuForUser(String userTypeCode) {
        UserType userType = UserType.fromCode(userTypeCode);
        MenuGroup menuGroup = (userType == UserType.ADMIN) ? MenuGroup.ADMIN : MenuGroup.REGULAR;

        List<MenuItem> items = menuItemRepository
                .findByMenuGroupAndActiveTrueOrderByDisplayOrder(menuGroup);

        List<MenuItemResponse> menuItems = items.stream()
                .map(MenuItemResponse::from)
                .toList();

        String menuType = (userType == UserType.ADMIN) ? "Admin Menu" : "Main Menu";

        return new MenuResponse(menuType, menuItems.size(), menuItems);
    }

    /**
     * Returns all active menu items across both menus.
     */
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findByActiveTrueOrderByMenuGroupAscDisplayOrderAsc()
                .stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    /**
     * Processes a menu navigation request, mirroring the PROCESS-ENTER-KEY paragraph.
     *
     * Business rules from COMEN01C.cbl:
     * 1. Validate option number is within range (1 to OPT-COUNT)
     * 2. Check user type access (regular users cannot access admin-only options)
     * 3. If program name starts with 'DUMMY', show "coming soon" message
     * 4. Otherwise, set COMMAREA fields and XCTL to target program
     */
    @Transactional
    public NavigationResponse navigate(NavigationRequest request) {
        UserType userType = UserType.fromCode(request.userType());
        MenuGroup menuGroup = (userType == UserType.ADMIN) ? MenuGroup.ADMIN : MenuGroup.REGULAR;

        MenuItem menuItem = menuItemRepository
                .findByMenuGroupAndOptionNumber(menuGroup, request.optionNumber())
                .orElseThrow(() -> new InvalidMenuOptionException(
                        "Please enter a valid option number..."));

        if (!menuItem.isActive()) {
            throw new InvalidMenuOptionException("Please enter a valid option number...");
        }

        // Check user type access (from COMEN01C.cbl lines 136-143)
        if (userType == UserType.USER && "A".equals(menuItem.getUserType())) {
            throw new UnauthorizedMenuAccessException("No access - Admin Only option...");
        }

        // Determine the source program name and tranid based on menu type
        String fromProgram = (userType == UserType.ADMIN) ? ADMIN_MENU_PROGRAM : REGULAR_MENU_PROGRAM;
        String fromTranid = (userType == UserType.ADMIN) ? ADMIN_MENU_TRANID : REGULAR_MENU_TRANID;

        // Update navigation context (COMMAREA equivalent)
        NavigationContext context = navigationContextRepository
                .findBySessionId(request.sessionId())
                .orElseGet(() -> new NavigationContext(request.sessionId()));

        context.setFromTranid(fromTranid);
        context.setFromProgram(fromProgram);
        context.setToProgram(menuItem.getProgramName());
        context.setUserType(request.userType().toUpperCase());
        context.setPgmContext(0);
        context.setLastUpdated(LocalDateTime.now());

        navigationContextRepository.save(context);

        return new NavigationResponse(
                request.sessionId(),
                fromProgram,
                menuItem.getProgramName(),
                menuItem.getProgramName(),
                menuItem.getOptionName(),
                0
        );
    }

    /**
     * Retrieves the current navigation context for a session.
     * Mirrors reading the CARDDEMO-COMMAREA from COCOM01Y.cpy.
     */
    @Transactional(readOnly = true)
    public NavigationContextResponse getNavigationContext(String sessionId) {
        NavigationContext context = navigationContextRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new NavigationContextNotFoundException(sessionId));

        return NavigationContextResponse.from(context);
    }

    /**
     * Handles the "back" navigation (PF3 key equivalent).
     * In COMEN01C.cbl, PF3 sets CDEMO-TO-PROGRAM to 'COSGN00C'
     * and performs RETURN-TO-SIGNON-SCREEN.
     */
    @Transactional
    public NavigationResponse navigateBack(String sessionId) {
        NavigationContext context = navigationContextRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new NavigationContextNotFoundException(sessionId));

        String previousProgram = context.getFromProgram();
        context.setToProgram("COSGN00C");
        context.setLastUpdated(LocalDateTime.now());
        navigationContextRepository.save(context);

        return new NavigationResponse(
                sessionId,
                previousProgram,
                "COSGN00C",
                "COSGN00C",
                "Sign On Screen",
                context.getPgmContext()
        );
    }
}
