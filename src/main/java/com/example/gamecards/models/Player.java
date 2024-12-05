package com.example.gamecards.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "players")
public class Player
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int hp = 10; // Starting player HP

    @Transient
    private Hero hero;

    @Transient
    private Card chosenCard;

}
