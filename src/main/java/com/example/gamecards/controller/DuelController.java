package com.example.gamecards.controller;

import com.example.gamecards.services.DuelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DuelController {

    @Autowired
    private DuelService duelService;

    @PostMapping("/duel/start")
    public ResponseEntity<?> startDuel(@RequestParam Long hero1Id, @RequestParam Long hero2Id) {
        try {
            duelService.startDuel(hero1Id, hero2Id);
            return ResponseEntity.ok("Duel started");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Duel start failed: " + e.getMessage());
        }
    }
}
