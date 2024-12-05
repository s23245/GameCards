package com.example.gamecards.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class StartDuelRequest
{
    private UUID gameId;

}