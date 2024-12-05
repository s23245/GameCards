package com.example.gamecards.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> users = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "game_session_heroes",
            joinColumns = @JoinColumn(name = "game_session_id"),
            inverseJoinColumns = @JoinColumn(name = "hero_id")
    )
    private List<Hero> heroes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "username")
    @Column(name = "hero_id")
    private Map<String, Long> selectedHeroes = new HashMap<>();

    @Column(nullable = false)
    private boolean duelStarted = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "game_session_id")
    private List<Player> players = new ArrayList<>();

    public GameSession(UUID id) {
        this.id = id;
    }

    // Methods
    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public Player getPlayer(String username) {
        return players.stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public boolean addUser(String username) {
        if (users.contains(username)) {
            return false; // User already in the session
        }
        users.add(username);
        return true;
    }

    public void selectHero(String username, Long heroId) {
        Hero hero = heroes.stream()
                .filter(h -> h.getId().equals(heroId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Hero not found"));
        selectedHeroes.put(username, heroId);
    }

    public boolean allHeroesSelected() {
        return users.size() == selectedHeroes.size();
    }

    public Hero getSelectedHero(String username) {
        Long heroId = selectedHeroes.get(username);
        if (heroId == null) return null;

        return heroes.stream()
                .filter(hero -> hero.getId().equals(heroId))
                .findFirst()
                .orElse(null);
    }
}