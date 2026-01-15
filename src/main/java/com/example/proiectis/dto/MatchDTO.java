package com.example.proiectis.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MatchDTO {
    private Long id;
    private Long player1Id;
    private Long player2Id;
    private Long winnerId;
    private int scorePlayer1;
    private int scorePlayer2;
    private String playedAt;
}
