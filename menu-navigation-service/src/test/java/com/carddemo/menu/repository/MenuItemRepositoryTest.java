package com.carddemo.menu.repository;

import com.carddemo.menu.entity.MenuGroup;
import com.carddemo.menu.entity.MenuItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MenuItemRepository, verifying Flyway-seeded data
 * matches the COMEN02Y.cpy and COADM02Y.cpy layouts.
 */
@DataJpaTest
class MenuItemRepositoryTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    void findRegularMenuItems_returns10Options() {
        List<MenuItem> items = menuItemRepository
                .findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup.REGULAR);

        assertThat(items).hasSize(10);
        assertThat(items.get(0).getOptionName()).isEqualTo("Account View");
        assertThat(items.get(0).getProgramName()).isEqualTo("COACTVWC");
        assertThat(items.get(9).getOptionName()).isEqualTo("Bill Payment");
        assertThat(items.get(9).getProgramName()).isEqualTo("COBIL00C");
    }

    @Test
    void findAdminMenuItems_returns4Options() {
        List<MenuItem> items = menuItemRepository
                .findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup.ADMIN);

        assertThat(items).hasSize(4);
        assertThat(items.get(0).getOptionName()).isEqualTo("User List (Security)");
        assertThat(items.get(0).getProgramName()).isEqualTo("COUSR00C");
        assertThat(items.get(3).getOptionName()).isEqualTo("User Delete (Security)");
        assertThat(items.get(3).getProgramName()).isEqualTo("COUSR03C");
    }

    @Test
    void findByMenuGroupAndOptionNumber_existingOption_returnsItem() {
        Optional<MenuItem> item = menuItemRepository
                .findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 6);

        assertThat(item).isPresent();
        assertThat(item.get().getOptionName()).isEqualTo("Transaction List");
        assertThat(item.get().getProgramName()).isEqualTo("COTRN00C");
    }

    @Test
    void findByMenuGroupAndOptionNumber_nonExistentOption_returnsEmpty() {
        Optional<MenuItem> item = menuItemRepository
                .findByMenuGroupAndOptionNumber(MenuGroup.REGULAR, 99);

        assertThat(item).isEmpty();
    }

    @Test
    void findAllActiveItems_returns14Total() {
        List<MenuItem> items = menuItemRepository
                .findByActiveTrueOrderByMenuGroupAscDisplayOrderAsc();

        assertThat(items).hasSize(14);
    }
}
