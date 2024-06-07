package com.example.gamecards.repositories;

import com.example.gamecards.models.Duel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DuelRepository extends JpaRepository<Duel, Long> {
}
