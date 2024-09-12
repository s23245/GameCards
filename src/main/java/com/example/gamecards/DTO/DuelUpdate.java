package com.example.gamecards.DTO;

import com.example.gamecards.models.Hero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DuelUpdate {
    private Hero hero1;
    private Hero hero2;

    public DuelUpdate(Hero hero1, Hero hero2) {
        this.hero1 = hero1;
        this.hero2 = hero2;
    }

}
