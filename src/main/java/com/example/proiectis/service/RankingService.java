package com.example.proiectis.service;

import com.example.proiectis.dto.RankingDTO;
import com.example.proiectis.entity.Player;
import com.example.proiectis.entity.Ranking;
import com.example.proiectis.repository.PlayerRepository;
import com.example.proiectis.repository.RankingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepo;
    private final PlayerRepository playerRepo;

    public RankingDTO getRanking(Long playerId) {
        Ranking r = rankingRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Ranking not found"));
        return toDTO(r);
    }

    public List<RankingDTO> getAllRankings() {
        return rankingRepo.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void updateRankingAfterMatch(Long winnerId, Long loserId) {

        // Folosim getReferenceById pentru entity managed
        Ranking winner = rankingRepo.findById(winnerId)
                .orElseGet(() -> createRanking(winnerId));

        Ranking loser = rankingRepo.findById(loserId)
                .orElseGet(() -> createRanking(loserId));

        winner.setWins(winner.getWins() + 1);
        loser.setLosses(loser.getLosses() + 1);

        winner.setTotalPoints(winner.getTotalPoints() + 3);

        winner.setWinRate(calcRate(winner));
        loser.setWinRate(calcRate(loser));

        rankingRepo.save(winner);
        rankingRepo.save(loser);
    }

    private double calcRate(Ranking r) {
        int total = r.getWins() + r.getLosses();
        return total == 0 ? 0 : (double) r.getWins() / total;
    }

    private Ranking createRanking(Long playerId) {
        // Folosim referință managed
        Player player = playerRepo.getReferenceById(playerId);
        Ranking r = Ranking.builder()
                .player(player)
                .wins(0)
                .losses(0)
                .totalPoints(0)
                .winRate(0)
                .build();
        return rankingRepo.save(r);
    }

    private RankingDTO toDTO(Ranking r) {
        return RankingDTO.builder()
                .playerId(r.getPlayer().getId())
                .username(r.getPlayer().getUsername())
                .wins(r.getWins())
                .losses(r.getLosses())
                .winRate(r.getWinRate())
                .totalPoints(r.getTotalPoints())
                .build();
    }
}