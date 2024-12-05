package com.example.gamecards.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "skills")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int manaCost;
    private int damage;
    private int cooldown;
    private int lastUsedRound = -1;

    private String type; // "passive" or "active"
    private String effect; // e.g., "hp_regen", "mana_regen", "damage"
    private int value; // Amount of HP or Mana to regen, or damage to deal
}