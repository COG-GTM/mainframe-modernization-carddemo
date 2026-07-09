package com.carddemo.web.menu;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.service.menu.MenuCatalog;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.CurrentUserProvider;
import com.carddemo.session.UserType;
import com.carddemo.web.menu.dto.MenuResponse;

/**
 * Read-only REST surface exposing the CardDemo online menus — the option lists that the
 * BMS screens of {@code COMEN01C}/{@code COADM01C} build via {@code BUILD-MENU-OPTIONS},
 * filtered for the authenticated user.
 *
 * <p>Option <em>selection</em> (choosing an option → {@code XCTL} to the target program) is
 * driven through the CS-3 navigation framework ({@code POST /api/nav} dispatching to the
 * registered menu {@link com.carddemo.session.ScreenHandler}s), not through this
 * controller.</p>
 *
 * <ul>
 *   <li>{@code GET /api/menu} — the main menu ({@code COMEN01C}); any authenticated user.</li>
 *   <li>{@code GET /api/menu/admin} — the admin menu ({@code COADM01C}); admins only
 *       ({@code 403} otherwise), mirroring the {@code COSGN00C} admin-vs-user routing.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuCatalog catalog;
    private final CurrentUserProvider currentUser;

    public MenuController(MenuCatalog catalog, CurrentUserProvider currentUser) {
        this.catalog = catalog;
        this.currentUser = currentUser;
    }

    /** The regular-user main menu, with options visible to the signed-on user type. */
    @GetMapping
    public MenuResponse mainMenu() {
        UserType userType = currentUser.userType().orElse(UserType.USER);
        return render(CardDemoProgram.MAIN_MENU, userType);
    }

    /** The admin menu; only an admin may reach it (the {@code CA00}/{@code COADM01C} gate). */
    @GetMapping("/admin")
    public ResponseEntity<MenuResponse> adminMenu() {
        UserType userType = currentUser.userType().orElse(UserType.USER);
        if (userType != UserType.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(render(CardDemoProgram.ADMIN_MENU, userType));
    }

    private MenuResponse render(CardDemoProgram menu, UserType userType) {
        return new MenuResponse(
            menu.name(),
            menu.tranId(),
            menu.programName(),
            menu.function(),
            currentUser.userId().orElse(null),
            userType.name(),
            catalog.visibleOptions(menu, userType));
    }
}
