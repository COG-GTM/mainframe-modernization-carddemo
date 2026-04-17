package com.carddemo.menu.service;

import com.carddemo.menu.dto.MenuResponse;
import com.carddemo.menu.dto.NavigationContextResponse;
import com.carddemo.menu.dto.NavigationRequest;
import com.carddemo.menu.dto.NavigationResponse;
import com.carddemo.menu.entity.MenuGroup;
import com.carddemo.menu.entity.MenuItem;
import com.carddemo.menu.entity.NavigationContext;
import com.carddemo.menu.exception.InvalidMenuOptionException;
import com.carddemo.menu.exception.NavigationContextNotFoundException;
import com.carddemo.menu.exception.UnauthorizedMenuAccessException;
import com.carddemo.menu.repository.MenuItemRepository;
import com.carddemo.menu.repository.NavigationContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MenuService, verifying business logic ported from
 * COMEN01C.cbl and COADM01C.cbl.
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private NavigationContextRepository navigationContextRepository;

    @InjectMocks
    private MenuService menuService;

    private MenuItem regularMenuItem;
    private MenuItem adminMenuItem;

    @BeforeEach
    void setUp() {
        regularMenuItem = new MenuItem(1, "Account View", "COACTVWC", "U",
                MenuGroup.REGULAR, 1);
        adminMenuItem = new MenuItem(1, "User List (Security)", "COUSR00C", "A",
                MenuGroup.ADMIN, 1);
    }

    // --- getMenuForUser tests ---

    @Test
    void getMenuForUser_regularUser_returnsRegularMenu() {
        when(menuItemRepository.findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup.REGULAR))
                .thenReturn(List.of(regularMenuItem));

        MenuResponse response = menuService.getMenuForUser("U");

        assertThat(response.menuType()).isEqualTo("Main Menu");
        assertThat(response.totalOptions()).isEqualTo(1);
        assertThat(response.menuItems()).hasSize(1);
        assertThat(response.menuItems().get(0).optionName()).isEqualTo("Account View");
    }

    @Test
    void getMenuForUser_adminUser_returnsAdminMenu() {
        when(menuItemRepository.findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup.ADMIN))
                .thenReturn(List.of(adminMenuItem));

        MenuResponse response = menuService.getMenuForUser("A");

        assertThat(response.menuType()).isEqualTo("Admin Menu");
        assertThat(response.totalOptions()).isEqualTo(1);
        assertThat(response.menuItems()).hasSize(1);
        assertThat(response.menuItems().get(0).optionName()).isEqualTo("User List (Security)");
    }

    @Test
    void getMenuForUser_invalidUserType_throwsException() {
        assertThatThrownBy(() -> menuService.getMenuForUser("X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown user type code");
    }

    @Test
    void getMenuForUser_caseInsensitive_returnsMenu() {
        when(menuItemRepository.findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup.REGULAR))
                .thenReturn(List.of(regularMenuItem));

        MenuResponse response = menuService.getMenuForUser("u");

        assertThat(response.menuType()).isEqualTo("Main Menu");
        assertThat(response.totalOptions()).isEqualTo(1);
    }

    @Test
    void getMenuForUser_emptyMenu_returnsEmptyList() {
        when(menuItemRepository.findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup.REGULAR))
                .thenReturn(List.of());

        MenuResponse response = menuService.getMenuForUser("U");

        assertThat(response.totalOptions()).isZero();
        assertThat(response.menuItems()).isEmpty();
    }

    // --- navigate tests ---

    @Test
    void navigate_validRegularOption_returnsNavigationResponse() {
        NavigationRequest request = new NavigationRequest("session-1", 1, "U");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 1))
                .thenReturn(Optional.of(regularMenuItem));
        when(navigationContextRepository.findBySessionId("session-1"))
                .thenReturn(Optional.empty());
        when(navigationContextRepository.save(any(NavigationContext.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NavigationResponse response = menuService.navigate(request);

        assertThat(response.fromProgram()).isEqualTo("COMEN01C");
        assertThat(response.toProgram()).isEqualTo("COACTVWC");
        assertThat(response.optionName()).isEqualTo("Account View");
        assertThat(response.pgmContext()).isZero();
    }

    @Test
    void navigate_validAdminOption_returnsAdminNavigationResponse() {
        NavigationRequest request = new NavigationRequest("session-2", 1, "A");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.ADMIN, 1))
                .thenReturn(Optional.of(adminMenuItem));
        when(navigationContextRepository.findBySessionId("session-2"))
                .thenReturn(Optional.empty());
        when(navigationContextRepository.save(any(NavigationContext.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NavigationResponse response = menuService.navigate(request);

        assertThat(response.fromProgram()).isEqualTo("COADM01C");
        assertThat(response.toProgram()).isEqualTo("COUSR00C");
    }

    @Test
    void navigate_invalidOption_throwsInvalidMenuOptionException() {
        NavigationRequest request = new NavigationRequest("session-3", 99, "U");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.navigate(request))
                .isInstanceOf(InvalidMenuOptionException.class)
                .hasMessageContaining("Please enter a valid option number");
    }

    @Test
    void navigate_regularUserAccessAdminOption_throwsUnauthorized() {
        MenuItem adminOnlyItem = new MenuItem(1, "Admin Feature", "ADMPRG01", "A",
                MenuGroup.REGULAR, 1);
        NavigationRequest request = new NavigationRequest("session-4", 1, "U");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 1))
                .thenReturn(Optional.of(adminOnlyItem));

        assertThatThrownBy(() -> menuService.navigate(request))
                .isInstanceOf(UnauthorizedMenuAccessException.class)
                .hasMessageContaining("No access - Admin Only option");
    }

    @Test
    void navigate_inactiveOption_throwsInvalidMenuOptionException() {
        MenuItem inactiveItem = new MenuItem(1, "Inactive", "INACT01", "U",
                MenuGroup.REGULAR, 1);
        inactiveItem.setActive(false);
        NavigationRequest request = new NavigationRequest("session-5", 1, "U");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 1))
                .thenReturn(Optional.of(inactiveItem));

        assertThatThrownBy(() -> menuService.navigate(request))
                .isInstanceOf(InvalidMenuOptionException.class);
    }

    @Test
    void navigate_dummyProgram_throwsComingSoonException() {
        MenuItem dummyItem = new MenuItem(11, "Future Feature", "DUMMY01C", "U",
                MenuGroup.REGULAR, 11);
        NavigationRequest request = new NavigationRequest("session-dummy", 11, "U");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 11))
                .thenReturn(Optional.of(dummyItem));

        assertThatThrownBy(() -> menuService.navigate(request))
                .isInstanceOf(InvalidMenuOptionException.class)
                .hasMessageContaining("coming soon");
    }

    @Test
    void navigate_existingSession_updatesContext() {
        NavigationContext existingCtx = new NavigationContext("session-6");
        existingCtx.setFromProgram("COSGN00C");

        NavigationRequest request = new NavigationRequest("session-6", 1, "U");
        when(menuItemRepository.findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 1))
                .thenReturn(Optional.of(regularMenuItem));
        when(navigationContextRepository.findBySessionId("session-6"))
                .thenReturn(Optional.of(existingCtx));
        when(navigationContextRepository.save(any(NavigationContext.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NavigationResponse response = menuService.navigate(request);

        assertThat(response.toProgram()).isEqualTo("COACTVWC");
        verify(navigationContextRepository).save(any(NavigationContext.class));
    }

    // --- getNavigationContext tests ---

    @Test
    void getNavigationContext_existingSession_returnsContext() {
        NavigationContext ctx = new NavigationContext("session-7");
        ctx.setUserId("USER0001");
        ctx.setUserType("U");
        ctx.setFromProgram("COMEN01C");
        ctx.setToProgram("COACTVWC");

        when(navigationContextRepository.findBySessionId("session-7"))
                .thenReturn(Optional.of(ctx));

        NavigationContextResponse response = menuService.getNavigationContext("session-7");

        assertThat(response.sessionId()).isEqualTo("session-7");
        assertThat(response.fromProgram()).isEqualTo("COMEN01C");
        assertThat(response.toProgram()).isEqualTo("COACTVWC");
    }

    @Test
    void getNavigationContext_nonExistentSession_throwsException() {
        when(navigationContextRepository.findBySessionId("nonexistent"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getNavigationContext("nonexistent"))
                .isInstanceOf(NavigationContextNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    // --- navigateBack tests ---

    @Test
    void navigateBack_existingSession_returnsSignonScreen() {
        NavigationContext ctx = new NavigationContext("session-8");
        ctx.setFromProgram("COMEN01C");
        ctx.setToProgram("COACTVWC");

        when(navigationContextRepository.findBySessionId("session-8"))
                .thenReturn(Optional.of(ctx));
        when(navigationContextRepository.save(any(NavigationContext.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NavigationResponse response = menuService.navigateBack("session-8");

        assertThat(response.toProgram()).isEqualTo("COSGN00C");
        assertThat(response.optionName()).isEqualTo("Sign On Screen");
    }

    @Test
    void navigateBack_nonExistentSession_throwsException() {
        when(navigationContextRepository.findBySessionId("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.navigateBack("missing"))
                .isInstanceOf(NavigationContextNotFoundException.class);
    }

    // --- getAllMenuItems tests ---

    @Test
    void getAllMenuItems_returnsAllActiveItems() {
        when(menuItemRepository.findByActiveTrueOrderByMenuGroupAscDisplayOrderAsc())
                .thenReturn(List.of(adminMenuItem, regularMenuItem));

        var items = menuService.getAllMenuItems();

        assertThat(items).hasSize(2);
    }
}
