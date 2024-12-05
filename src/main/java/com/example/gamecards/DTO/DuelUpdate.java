package com.example.gamecards.DTO;

import com.example.gamecards.models.Hero;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DuelUpdate {
    private String user1;
    private Hero hero1;
    private String user2;
    private Hero hero2;
    private List<String> logs;
}