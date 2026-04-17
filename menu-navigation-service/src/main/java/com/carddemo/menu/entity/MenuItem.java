package com.carddemo.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a menu option.
 * Maps to the REDEFINES structure in COMEN02Y.cpy / COADM02Y.cpy:
 * <pre>
 *   15 CDEMO-MENU-OPT-NUM      PIC 9(02)
 *   15 CDEMO-MENU-OPT-NAME     PIC X(35)
 *   15 CDEMO-MENU-OPT-PGMNAME  PIC X(08)
 *   15 CDEMO-MENU-OPT-USRTYPE  PIC X(01)
 * </pre>
 */
@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_number", nullable = false)
    private int optionNumber;

    @Column(name = "option_name", nullable = false, length = 35)
    private String optionName;

    @Column(name = "program_name", nullable = false, length = 8)
    private String programName;

    @Column(name = "user_type", nullable = false, length = 1)
    private String userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_group", nullable = false, length = 10)
    private MenuGroup menuGroup;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected MenuItem() {
    }

    public MenuItem(int optionNumber, String optionName, String programName,
                    String userType, MenuGroup menuGroup, int displayOrder) {
        this.optionNumber = optionNumber;
        this.optionName = optionName;
        this.programName = programName;
        this.userType = userType;
        this.menuGroup = menuGroup;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public int getOptionNumber() {
        return optionNumber;
    }

    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public MenuGroup getMenuGroup() {
        return menuGroup;
    }

    public void setMenuGroup(MenuGroup menuGroup) {
        this.menuGroup = menuGroup;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
