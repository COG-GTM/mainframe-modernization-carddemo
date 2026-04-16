package com.carddemo.menu.repository;

import com.carddemo.menu.entity.MenuGroup;
import com.carddemo.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for menu item data access.
 * Provides query methods matching the COBOL BUILD-MENU-OPTIONS paragraph's
 * iteration over CDEMO-MENU-OPT-COUNT options filtered by user type.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByMenuGroupAndActiveTrueOrderByDisplayOrder(MenuGroup menuGroup);

    List<MenuItem> findByUserTypeAndActiveTrueOrderByDisplayOrder(String userType);

    List<MenuItem> findByMenuGroupAndUserTypeAndActiveTrueOrderByDisplayOrder(
            MenuGroup menuGroup, String userType);

    Optional<MenuItem> findByMenuGroupAndOptionNumber(MenuGroup menuGroup, int optionNumber);

    List<MenuItem> findByActiveTrueOrderByMenuGroupAscDisplayOrderAsc();
}
