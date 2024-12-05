package com.example.gamecards.services;

import com.example.gamecards.models.GameSession;
import com.example.gamecards.models.Hero;
import com.example.gamecards.models.User;
import com.example.gamecards.repositories.GameSessionRepository;
import com.example.gamecards.repositories.HeroRepository;
import com.example.gamecards.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameService
{
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private HeroRepository heroRepository;

    @Autowired
    private UserRepository userRepository;

    public GameSession createGameSession() {
        GameSession newGameSession = new GameSession();
        List<Hero> allHeroes = heroRepository.findAll();
        newGameSession.setHeroes(allHeroes);
        return gameSessionRepository.save(newGameSession);
    }

    public List<GameSession> getActiveGames() {
        return gameSessionRepository.findAll();
    }

    public GameSession getGameSession(UUID gameId) {
        return gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));
    }

    public GameSession joinGameSession(UUID gameId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<GameSession> gameSessionOptional = gameSessionRepository.findById(gameId);
        if (gameSessionOptional.isPresent()) {
            GameSession gameSession = gameSessionOptional.get();
            if (gameSession.addUser(username)) {
                logger.info("User added to session: {}", username);
                return gameSessionRepository.save(gameSession);
            } else {
                logger.warn("User already in the game session or game session is full: {}", username);
                throw new IllegalStateException("User already in the game session or game session is full");
            }
        } else {
            throw new IllegalArgumentException("Game session not found");
        }
    }

    public GameSession searchGame() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<GameSession> activeGames = gameSessionRepository.findAll();
        for (GameSession gameSession : activeGames) {
            if (gameSession.addUser(username)) {
                logger.info("User added to existing session: {}", gameSession.getId());
                return gameSessionRepository.save(gameSession);
            }
        }
        GameSession newGameSession = createGameSession();
        newGameSession.addUser(username);
        logger.info("Created new game session: {}", newGameSession.getId());
        return gameSessionRepository.save(newGameSession);
    }

    public GameSession selectHero(UUID gameId, Long heroId) {
        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);

        if(user.isEmpty())
            throw new UsernameNotFoundException("User nor found");

        gameSession.selectHero(user.get().getUsername(), heroId);

        gameSessionRepository.save(gameSession);
        return gameSession;
    }



    private String extractUsername(String usernameJson) {
        try {
            return new ObjectMapper().readTree(usernameJson).get("username").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid username JSON format", e);
        }
    }

    public boolean allUsersSelectedHeroes(UUID gameId) {
        GameSession gameSession = getGameSession(gameId);
        return gameSession.allHeroesSelected();
    }
}
