package com.example.gamecards.controller;

import com.example.gamecards.models.GameSession;
import com.example.gamecards.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/search")
    public GameSession searchGame(@RequestBody String usernameJson) {
        return gameService.searchGame(usernameJson);
    }

    @PostMapping("/joinGame")
    public ResponseEntity<GameSession> joinGame(@RequestParam UUID gameId, @RequestBody String usernameJson) {
        try {
            GameSession gameSession = gameService.joinGameSession(gameId, usernameJson);
            return ResponseEntity.ok(gameSession);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PostMapping("/selectHero")
    public ResponseEntity<String> selectHero(@RequestParam UUID gameId, @RequestParam Long heroId) {
        try {
            GameSession gameSession = gameService.selectHero(gameId, heroId);
            return ResponseEntity.ok("Hero selected successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to select this hero");
        }
    }

    @GetMapping("/{gameId}")
    public GameSession getGameSession(@PathVariable UUID gameId) {
        return gameService.getGameSession(gameId);
    }
}


