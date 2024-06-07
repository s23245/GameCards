package com.example.gamecards.DTO;

import lombok.Data;

@Data
public class HeroDTO {
    private Long id;
    private String name;
    private int hp;
    private int mana;
    private String abilities;
    private String mainElement;
}
