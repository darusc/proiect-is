package com.example.proiectis.service;

import com.example.proiectis.dto.PlayerDTO;
import com.example.proiectis.dto.CreatePlayerRequest;
import com.example.proiectis.entity.Player;
import com.example.proiectis.repository.PlayerRepository;
import com.example.proiectis.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepo;

    public PlayerDTO register(CreatePlayerRequest request) {
        Player player = Player.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        playerRepo.save(player);

        return toDTO(player);
    }

    public PlayerDTO getPlayer(Long id) {
        return playerRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public List<PlayerDTO> getAllPlayers() {
        return playerRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private PlayerDTO toDTO(Player p) {
        return PlayerDTO.builder()
                .id(p.getId())
                .username(p.getUsername())
                .email(p.getEmail())
                .build();
    }
}
