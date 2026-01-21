package com.example.proiectis.controller;

import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.MatchRepository;
import com.example.proiectis.repository.RankingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LobbyController {

    @Autowired private RankingRepository rankingRepository;
    @Autowired private MatchRepository matchRepository;

    @GetMapping("/")
    public String lobby(HttpSession session, Model model) {
        Player loggedInPlayer = (Player) session.getAttribute("user");

        if (loggedInPlayer == null) {
            return "redirect:/login";
        }

        // Trimitem playerId (pentru JS-ul tău original) și currentUserId (pentru HTML)
        model.addAttribute("playerId", loggedInPlayer.getId());
        model.addAttribute("currentUserId", loggedInPlayer.getId());
        model.addAttribute("username", loggedInPlayer.getUsername());

        // Datele pentru cele două coloane laterale
        model.addAttribute("globalRankings", rankingRepository.findAllByOrderByTotalPointsDesc());
        model.addAttribute("matchHistory", matchRepository.findByPlayer1OrPlayer2OrderByPlayedAtDesc(loggedInPlayer, loggedInPlayer));

        return "lobby";
    }
}