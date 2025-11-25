package com.example.proiectis.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatePlayerRequest {
    private String username;
    private String email;
    private String password;
}
