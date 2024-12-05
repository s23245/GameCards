package com.example.gamecards.controller;

import com.example.gamecards.DTO.CardSelectionRequest;
import com.example.gamecards.DTO.StartDuelRequest;
import com.example.gamecards.DTO.DuelUpdate;
import com.example.gamecards.models.GameSession;
import com.example.gamecards.repositories.GameSessionRepository;
import com.example.gamecards.services.DuelService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/duel")
public class DuelController
{
    private Map<UUID, Set<String>> connectedPlayers = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(DuelController.class);


    @Autowired
    private DuelService duelService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @MessageMapping("/player-ready")
    public void playerReady(Map<String, String> payload) {
        String gameIdStr = payload.get("gameId");
        String username = payload.get("username");
        UUID gameId = UUID.fromString(gameIdStr);

        duelService.addConnectedPlayer(gameId, username);
        logger.info("Player {} is ready for game {}", username, gameId);

        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));

        if (duelService.getConnectedPlayers(gameId).containsAll(gameSession.getUsers())) {
            // All players are ready, start the game loop
            duelService.startGameSession(gameId);
        }
    }
    @PostMapping("/start")
    public ResponseEntity<String> startDuel(@RequestBody StartDuelRequest request, @RequestHeader("Authorization") String token) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        duelService.startGameSession(request.getGameId());
        return ResponseEntity.ok("Duel started successfully");
    }

    @PostMapping("/choose-card")
    public ResponseEntity<Map<String, Object>> chooseCard(@RequestBody CardSelectionRequest request) {
        Map<String, Object> response = duelService.applyChosenCard(request.getCardId(), request.getGameId());
        return ResponseEntity.ok(response);
    }



}