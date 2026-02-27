package com.carddemo.controller;

import com.carddemo.dto.MenuOption;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    @GetMapping
    public ResponseEntity<List<MenuOption>> getMenu() {
        List<MenuOption> options = List.of(
            new MenuOption(1, "CAVW", "View Account"),
            new MenuOption(2, "CAUP", "Update Account"),
            new MenuOption(3, "CCLI", "Card List"),
            new MenuOption(4, "CCDL", "Card Detail"),
            new MenuOption(5, "CCUP", "Update Card"),
            new MenuOption(6, "CT00", "Transaction List"),
            new MenuOption(7, "CT01", "Transaction Detail"),
            new MenuOption(8, "CT02", "New Transaction"),
            new MenuOption(9, "CR00", "Reports"),
            new MenuOption(10, "CB00", "Bill Payment"),
            new MenuOption(11, "CA00", "Admin Menu")
        );
        return ResponseEntity.ok(options);
    }
}
