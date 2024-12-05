package com.example.gamecards.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "duel")
public class Duel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hero1_id")
    private Hero hero1;

    @ManyToOne
    @JoinColumn(name = "hero2_id")
    private Hero hero2;

    private String result;
}
