package com.example.proiectis.service;

import com.example.proiectis.dto.MatchDTO;
import com.example.proiectis.dto.CreatePlayerRequest;
import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MatchServiceTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PlayerRepository playerRepository;

    private Player player1, player2;

    @BeforeEach
    void setup() {
        playerRepository.deleteAll();
        player1 = playerRepository.save(Player.builder()
                .username("player1")
                .email("p1@example.com")
                .password("pass")
                .level(1)
                .score(0)
                .build());
        player2 = playerRepository.save(Player.builder()
                .username("player2")
                .email("p2@example.com")
                .password("pass")
                .level(1)
                .score(0)
                .build());
    }

    @Test
    void testRecordMatch() {
        MatchDTO dto = MatchDTO.builder()
                .player1Id(player1.getId())
                .player2Id(player2.getId())
                .winnerId(player1.getId())
                .scorePlayer1(5)
                .scorePlayer2(3)
                .history("[\"move1\",\"move2\"]")
                .build();

        MatchDTO saved = matchService.recordMatch(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWinnerId()).isEqualTo(player1.getId());
    }
}
