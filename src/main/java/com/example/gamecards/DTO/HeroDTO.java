package com.example.gamecards.DTO;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HeroDTO
{
    private String name;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private int attack;
    private int defense;
    private int attackDamage;
    private int attackSpeed;
    private String mainElement;
    private String imageUrl;
}
