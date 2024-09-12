package com.example.gamecards.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StartDuelRequest
{
    private UUID gameId;

}