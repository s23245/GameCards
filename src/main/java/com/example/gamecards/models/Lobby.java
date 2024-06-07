package com.example.gamecards.models;

import com.example.gamecards.models.Hero;
import com.example.gamecards.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "lobby")
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int maxPlayers;

    @ManyToMany
    @JoinTable(
            name = "lobby_users",
            joinColumns = @JoinColumn(name = "lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> players = new HashSet<>();

    @OneToMany(mappedBy = "lobby")
    private Set<Hero> heroes = new HashSet<>();
}
