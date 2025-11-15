package com.example.proiectis.service;

import com.example.proiectis.dto.MatchDTO;
import com.example.proiectis.entity.Match;
import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.MatchRepository;
import com.example.proiectis.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepo;
    private final PlayerRepository playerRepo;

    @Transactional
    public MatchDTO recordMatch(MatchDTO dto) {

        // Folosim getReferenceById ca să avem entity managed
        Player p1 = playerRepo.getReferenceById(dto.getPlayer1Id());
        Player p2 = playerRepo.getReferenceById(dto.getPlayer2Id());
        Player winner = playerRepo.getReferenceById(dto.getWinnerId());

        Match match = Match.builder()
                .player1(p1)
                .player2(p2)
                .winner(winner)
                .scorePlayer1(dto.getScorePlayer1())
                .scorePlayer2(dto.getScorePlayer2())
                .playedAt(LocalDateTime.now())
                .history(dto.getHistory())
                .build();

        matchRepo.save(match);

        return toDTO(match);
    }

    public MatchDTO getMatch(Long id) {
        Match m = matchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return toDTO(m);
    }

    private MatchDTO toDTO(Match m) {
        return MatchDTO.builder()
                .id(m.getId())
                .player1Id(m.getPlayer1().getId())
                .player2Id(m.getPlayer2().getId())
                .winnerId(m.getWinner().getId())
                .scorePlayer1(m.getScorePlayer1())
                .scorePlayer2(m.getScorePlayer2())
                .playedAt(m.getPlayedAt().toString())
                .history(m.getHistory())
                .build();
    }
}
