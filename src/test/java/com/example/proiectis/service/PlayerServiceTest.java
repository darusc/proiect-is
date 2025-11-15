package com.example.proiectis.service;

import com.example.proiectis.dto.CreatePlayerRequest;
import com.example.proiectis.dto.PlayerDTO;
import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setup() {
        playerRepository.deleteAll();
    }

    @Test
    void testRegisterPlayer() {
        CreatePlayerRequest request = new CreatePlayerRequest("testuser", "test@example.com", "pass123");
        PlayerDTO dto = playerService.register(request);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getScore()).isEqualTo(0);
        assertThat(dto.getLevel()).isEqualTo(1);
    }

    @Test
    void testGetAllPlayers() {
        playerService.register(new CreatePlayerRequest("user1", "u1@example.com", "pass1"));
        playerService.register(new CreatePlayerRequest("user2", "u2@example.com", "pass2"));

        var players = playerService.getAllPlayers();
        assertThat(players).hasSize(2);
    }
}
