package com.example.gamecards.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class HeroSelectionRequest
{
    private UUID gameId;
    private Long heroId;

}