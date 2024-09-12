package com.example.gamecards.repositories;

import com.example.gamecards.models.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession,Long> {
    Optional<GameSession> findById(UUID gameId);
}
