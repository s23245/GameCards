package com.example.gamecards.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeroUpdate
{
    @JsonProperty("hero")
    private HeroDTO hero;

    @JsonProperty("attributeChanges")
    private List<String> attributeChanges;
}