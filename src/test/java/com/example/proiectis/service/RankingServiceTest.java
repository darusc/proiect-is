package com.example.proiectis.service;

import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RankingServiceTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RankingService rankingService;

    private Player winner, loser;

    @BeforeEach
    void setup() {
        playerRepository.deleteAll();
        winner = playerRepository.save(Player.builder()
                .username("winner")
                .email("winner@example.com")
                .password("pass")
                .level(1)
                .score(0)
                .build());
        loser = playerRepository.save(Player.builder()
                .username("loser")
                .email("loser@example.com")
                .password("pass")
                .level(1)
                .score(0)
                .build());
    }

    @Test
    void testUpdateRankingAfterMatch() {
        rankingService.updateRankingAfterMatch(winner.getId(), loser.getId());

        var winnerRanking = rankingService.getRanking(winner.getId());
        var loserRanking = rankingService.getRanking(loser.getId());

        assertThat(winnerRanking.getWins()).isEqualTo(1);
        assertThat(loserRanking.getLosses()).isEqualTo(1);
        assertThat(winnerRanking.getTotalPoints()).isEqualTo(3);
    }
}
