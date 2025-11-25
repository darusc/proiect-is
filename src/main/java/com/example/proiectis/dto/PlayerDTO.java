package com.example.proiectis.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PlayerDTO {
    private Long id;
    private String username;
    private String email;
    private int level;
    private int score;
}
