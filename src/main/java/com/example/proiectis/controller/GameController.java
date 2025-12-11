package com.example.proiectis.controller;

import com.example.proiectis.game.LobbyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/game")
public class GameController {

    private final LobbyManager lobbyManager;

    public GameController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @GetMapping("/{id}")
    public String game(
            @PathVariable String id,
            @RequestParam(name = "player") Long playerId, /// TO DO autentificare
            Model model
    ) {
        model.addAttribute("roomId", id);
        model.addAttribute("playerId", playerId);

        if(!lobbyManager.isPlayerInRoom(id, playerId)) {
            return "redirect:/";
        }

        return "game";
    }
}
