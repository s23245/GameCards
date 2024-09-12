package com.example.gamecards.models;

import com.example.gamecards.models.Lobby;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hero")
public class Hero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private int hp;
    private int mana;
    private String abilities;
    private String mainElement;

    private int attack;
    private int defense;
    private int attackDamage;
    private int attackSpeed;

    @ManyToOne
    @JoinColumn(name = "lobby_id")
    private Lobby lobby;

    private String imageUrl;
}
