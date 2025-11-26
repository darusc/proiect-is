package com.example.proiectis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class LobbyController {

    @GetMapping("/")
    public String lobby(
            @RequestParam(name = "player") Long playerId, /// TO DO autentificare
            Model model
    ) {
        model.addAttribute("playerId", playerId);
        return "lobby";
    }
}
