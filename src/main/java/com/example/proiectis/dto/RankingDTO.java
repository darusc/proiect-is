package com.example.proiectis.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RankingDTO {
    private Long playerId;
    private String username;
    private int wins;
    private int losses;
    private double winRate;
    private int totalPoints;
}
