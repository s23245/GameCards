package com.example.gamecards.services;

import com.example.gamecards.DTO.DuelUpdate;
import com.example.gamecards.models.GameSession;
import com.example.gamecards.models.Hero;
import com.example.gamecards.models.Player;
import com.example.gamecards.models.User;
import com.example.gamecards.repositories.GameSessionRepository;
import com.example.gamecards.repositories.HeroRepository;
import com.example.gamecards.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class DuelService
{

    private static final Logger logger = LoggerFactory.getLogger(DuelService.class);

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HeroRepository heroRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    public void startDuel(UUID gameId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        String username = user.getUsername();

        logger.info("Starting duel for gameId: {} by user: {}", gameId, username);

        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));

        if (gameSession.isDuelStarted()) {
            throw new IllegalStateException("Duel already started for this game session");
        }

        boolean userInSession = gameSession.getUsers().stream()
                .anyMatch(userJson -> extractUsername(userJson).equals(username));

        if (!userInSession) {
            throw new IllegalArgumentException("User not part of this game session");
        }

        List<Player> players = new ArrayList<>();
        for (String userJson : gameSession.getUsers()) {
            Long heroId = gameSession.getSelectedHeroes().get(userJson);
            if (heroId != null) {
                Hero hero = gameSession.getHeroes().stream()
                        .filter(h -> h.getId().equals(heroId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Hero not found"));
                players.add(new Player(extractUsername(userJson), hero));
            }
        }

        if (players.size() != 2) {
            throw new IllegalArgumentException("Duel requires exactly two players");
        }

        Player player1 = players.get(0);
        Player player2 = players.get(1);
        runBattle(player1.getHero(), player2.getHero(), gameId);

        gameSession.setDuelStarted(true);
        gameSessionRepository.save(gameSession);
    }

    private void runBattle(Hero hero1, Hero hero2, UUID gameId) {
        int initialHpHero1 = hero1.getHp();
        int initialHpHero2 = hero2.getHp();
        sendDuelUpdate(hero1, hero2, gameId);
        while (hero1.getHp() > 0 && hero2.getHp() > 0) {
            attack(hero1, hero2);
            if (hero2.getHp() > 0) {
                attack(hero2, hero1);
            }
            sendDuelUpdate(hero1, hero2, gameId);
        }
        String result = hero1.getHp() > 0 ? hero1.getName() + " wins!" : hero2.getName() + " wins!";
        messagingTemplate.convertAndSend("/topic/duel-result/" + gameId, result);

        // Reset HP to initial values
        hero1.setHp(initialHpHero1);
        hero2.setHp(initialHpHero2);

        // Save the updated heroes
        heroRepository.save(hero1);
        heroRepository.save(hero2);
    }

    private void attack(Hero attacker, Hero defender) {
        int damage = Math.max(0, attacker.getAttackDamage() - defender.getDefense());
        defender.setHp(defender.getHp() - damage);
        logger.info("{} attacks {} for {} damage", attacker.getName(), defender.getName(), damage);
    }

    private void sendDuelUpdate(Hero hero1, Hero hero2, UUID gameId) {
        logger.info("Sending duel update for gameId {}: hero1: {}, hero2: {}", gameId, hero1, hero2);
        DuelUpdate duelUpdate = new DuelUpdate(hero1, hero2);
        messagingTemplate.convertAndSend("/topic/duel-progress/" + gameId, duelUpdate);
    }


    public DuelUpdate getDuelData(UUID gameId) {
        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));

        // Ensure heroes are selected
        if (gameSession.getSelectedHeroes().size() < 2) {
            throw new IllegalArgumentException("Two heroes must be selected for the duel");
        }

        List<Hero> selectedHeroes = new ArrayList<>(gameSession.getHeroes());
        Hero hero1 = selectedHeroes.get(0);
        Hero hero2 = selectedHeroes.get(1);

        return new DuelUpdate(hero1, hero2);
    }

    private String extractUsername(String usernameJson) {
        try {
            return new ObjectMapper().readTree(usernameJson).get("username").asText();
        } catch (Exception e) {
            logger.error("Invalid username JSON format: {}", usernameJson, e);
            throw new IllegalArgumentException("Invalid username JSON format", e);
        }
    }
}
