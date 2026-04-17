package com.carddemo.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for menu options, replacing the COBOL copybooks
 * {@code COMEN02Y.cpy} (user menu) and {@code COADM02Y.cpy} (admin menu).
 *
 * <p>The COBOL application hard-codes menu options as FILLER entries in
 * copybook data structures. This class externalizes them to YAML configuration,
 * allowing changes without recompilation.</p>
 *
 * <p>Loaded from {@code application.yml} under the {@code carddemo.menu} prefix.</p>
 */
@Validated
@ConfigurationProperties(prefix = "carddemo.menu")
public class MenuConfigurationProperties {

    /** Menu options available to regular users (from COMEN02Y.cpy). */
    private List<@Valid MenuOption> userMenu = new ArrayList<>();

    /** Menu options available to admin users (from COADM02Y.cpy). */
    private List<@Valid MenuOption> adminMenu = new ArrayList<>();

    public List<MenuOption> getUserMenu() {
        return userMenu;
    }

    public void setUserMenu(List<MenuOption> userMenu) {
        this.userMenu = userMenu;
    }

    public List<MenuOption> getAdminMenu() {
        return adminMenu;
    }

    public void setAdminMenu(List<MenuOption> adminMenu) {
        this.adminMenu = adminMenu;
    }

    /**
     * A single menu option entry.
     *
     * <p>Maps to the COBOL structure:</p>
     * <pre>
     *   15 CDEMO-MENU-OPT-NUM     PIC 9(02).
     *   15 CDEMO-MENU-OPT-NAME    PIC X(35).
     *   15 CDEMO-MENU-OPT-PGMNAME PIC X(08).
     *   15 CDEMO-MENU-OPT-USRTYPE PIC X(01).
     * </pre>
     */
    public static class MenuOption {

        /** Menu option number displayed to the user. */
        @Positive
        private int option;

        /** Display label for the menu option. */
        @NotBlank
        private String label;

        /** Original COBOL program name (for traceability). */
        @NotBlank
        private String program;

        /** Modern REST API route this option maps to. */
        @NotBlank
        private String route;

        public int getOption() {
            return option;
        }

        public void setOption(int option) {
            this.option = option;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getProgram() {
            return program;
        }

        public void setProgram(String program) {
            this.program = program;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }
    }
}
