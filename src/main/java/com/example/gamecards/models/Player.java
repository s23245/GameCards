package com.example.gamecards.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Player
{
    private String username;
    private Hero hero;

    public Player(String username, Hero hero) {
        this.username = username;
        this.hero = hero;
    }

}
