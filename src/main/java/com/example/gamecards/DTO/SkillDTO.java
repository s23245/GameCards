package com.example.gamecards.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO
{
    private Long id;
    private String name;
    private int manaCost;
    private int damage;
    private int cooldown;
    private int lastUsedRound;

    private String type;
    private String effect;
    private int value;
}
