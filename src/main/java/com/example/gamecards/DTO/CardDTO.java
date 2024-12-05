package com.example.gamecards.DTO;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CardDTO
{
    private Long id;
    private String name;
    private String description;
    private String rarity;
    private String imageUrl;
    private Map<String, Integer> attributes;
    private List<SkillDTO> skills;
}
