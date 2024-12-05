package com.example.gamecards.DTO;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatus
{
    private String username;
    private int hp;
}