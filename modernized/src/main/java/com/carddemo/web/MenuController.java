package com.carddemo.web;

import com.carddemo.service.MenuService;
import com.carddemo.web.dto.MenuOption;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** COMEN01C (main menu) and COADM01C (admin menu). */
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/main")
    public List<MenuOption> mainMenu() {
        return menuService.mainMenu();
    }

    @GetMapping("/admin")
    public List<MenuOption> adminMenu() {
        return menuService.adminMenu();
    }

    @GetMapping("/main/{option}")
    public MenuOption selectMain(@PathVariable Integer option) {
        return menuService.select(menuService.mainMenu(), option);
    }

    @GetMapping("/admin/{option}")
    public MenuOption selectAdmin(@PathVariable Integer option) {
        return menuService.select(menuService.adminMenu(), option);
    }
}
