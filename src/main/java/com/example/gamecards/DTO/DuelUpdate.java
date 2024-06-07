package com.example.gamecards.DTO;

import com.example.gamecards.models.Hero;

public class DuelUpdate {
    private Hero hero1;
    private Hero hero2;

    public DuelUpdate(Hero hero1, Hero hero2) {
        this.hero1 = hero1;
        this.hero2 = hero2;
    }

    public Hero getHero1() {
        return hero1;
    }

    public Hero getHero2() {
        return hero2;
    }
}
