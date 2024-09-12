package com.example.gamecards.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ElementCollection
    private List<String> users = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "game_session_heroes",
            joinColumns = @JoinColumn(name = "game_session_id"),
            inverseJoinColumns = @JoinColumn(name = "hero_id")
    )
    private List<Hero> heroes = new ArrayList<>();

    @ElementCollection
    private Map<String, Long> selectedHeroes = new HashMap<>();

    @Builder.Default
    private boolean duelStarted = false;

    public boolean addUser(String usernameJson) {
        String username = extractUsername(usernameJson);
        for (String user : users) {
            if (extractUsername(user).equals(username)) {
                return false; // User already in the session
            }
        }
        users.add(usernameJson);
        return true;
    }

    public void selectHero(String usernameJson, Long heroId) {
        Hero hero = heroes.stream().filter(h -> h.getId().equals(heroId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Hero not found"));
        selectedHeroes.put(usernameJson, heroId);
    }

    private String extractUsername(String usernameJson) {
        try {
            return new ObjectMapper().readTree(usernameJson).get("username").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid username JSON format", e);
        }
    }

    public boolean allHeroesSelected() {
        return users.size() == selectedHeroes.size();
    }

    public Hero getSelectedHero(String usernameJson)
    {
        Long heroId = selectedHeroes.get(usernameJson);
        if (heroId == null) return null;

        return heroes.stream()
                .filter(hero -> hero.getId().equals(heroId))
                .findFirst()
                .orElse(null);
    }
}
