package com.example.gamecards.DTO;
import lombok.Data;

import java.util.UUID;

@Data
public class CardSelectionRequest {
    private Long cardId;
    private UUID gameId;
}