package com.example.gamecards.controller;

import com.example.gamecards.DTO.StartDuelRequest;
import com.example.gamecards.DTO.DuelUpdate;
import com.example.gamecards.services.DuelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/duel")
public class DuelController {

    private static final Logger logger = LoggerFactory.getLogger(DuelController.class);

    @Autowired
    private DuelService duelService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/start")
    public ResponseEntity<String> startDuel(@RequestBody StartDuelRequest request) {
        logger.info("Received startDuel request with gameId: {}", request.getGameId());
        try {
            duelService.startDuel(request.getGameId());
            messagingTemplate.convertAndSend("/topic/duel-progress/" + request.getGameId(), "DUEL_STARTED");
            logger.info("Duel started successfully for gameId: {}", request.getGameId());
            return ResponseEntity.ok("Duel started successfully");
        } catch (Exception e) {
            logger.error("Duel start failed for gameId: {}: {}", request.getGameId(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Duel start failed: " + e.getMessage());
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<DuelUpdate> getDuelData(@PathVariable UUID gameId) {
        logger.info("Received getDuelData request for gameId: {}", gameId);
        try {
            DuelUpdate duelUpdate = duelService.getDuelData(gameId);
            logger.info("Duel data retrieved successfully for gameId: {}", gameId);
            return ResponseEntity.ok(duelUpdate);
        } catch (Exception e) {
            logger.error("Failed to get duel data for gameId: {}: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
