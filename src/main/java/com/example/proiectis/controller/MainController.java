package com.example.proiectis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/game")
public class MainController {

    @GetMapping("/")
    public String home() {
        return "game-join";
    }

    @PostMapping("/create")
    public String createGame() {
        String newGameId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "redirect:/game/" + newGameId;
    }

    @PostMapping("/join")
    public String join(@RequestParam String gameId) {
        return "redirect:/game/" + gameId;
    }

    @GetMapping("/{id}")
    public String game(@PathVariable String id, Model model) {
        model.addAttribute("roomId", id);
        return "game";
    }
}
