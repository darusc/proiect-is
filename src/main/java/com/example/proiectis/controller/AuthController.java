package com.example.proiectis.controller;

import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Optional<Player> player = playerRepository.findByUsername(username);

        if (player.isPresent() && player.get().getPassword().equals(password)) {
            session.setAttribute("user", player.get());
            return "redirect:/";
        }

        model.addAttribute("error", "Username sau parolă incorectă!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         Model model) {

        if (playerRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Acest username este deja ocupat!");
            return "signup";
        }

        Player newPlayer = Player.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();

        playerRepository.save(newPlayer); // Salvare în MySQL

        return "redirect:/login?signupSuccess=true";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }
}