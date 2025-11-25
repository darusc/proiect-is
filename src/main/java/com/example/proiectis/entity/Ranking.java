package com.example.proiectis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rankings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Ranking {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "player_id")
    private Player player;

    private int wins;
    private int losses;
    private double winRate;
    private int totalPoints;
}
