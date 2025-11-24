package com.example.proiectis.repository;

import com.example.proiectis.entity.Match;
import com.example.proiectis.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPlayer1OrPlayer2(Player p1, Player p2);
}
