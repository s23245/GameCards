package com.example.gamecards.models;

import com.example.gamecards.DTO.CardDTO;
import com.example.gamecards.DTO.SkillDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "hero")
public class Hero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private int attack;
    private int defense;
    private int attackDamage;
    private int attackSpeed;
    private String mainElement;
    private String imageUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "hero_skills",
            joinColumns = @JoinColumn(name = "hero_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills = new ArrayList<>();

    // Constructors, getters, setters...

    // Method to apply card effects to the hero
    public void applyCard(CardDTO cardDTO, List<String> attributeChanges) {
        String rarity = cardDTO.getRarity().toLowerCase();

        switch (rarity) {
            case "common":
                // Small attribute increases
                applyAttributes(cardDTO.getAttributes(), attributeChanges);
                break;
            case "uncommon":
                // Basic attributes and basic passive skills
                applyAttributes(cardDTO.getAttributes(), attributeChanges);
                addSkills(cardDTO.getSkills(), "passive");
                break;
            case "rare":
                // Good attribute and basic active skill
                applyAttributes(cardDTO.getAttributes(), attributeChanges);
                addSkills(cardDTO.getSkills(), "active");
                break;
            case "epic":
                // Good attribute and great active skill
                applyAttributes(cardDTO.getAttributes(), attributeChanges);
                addSkills(cardDTO.getSkills(), "active");
                break;
            case "legendary":
                // Exclusive active skills that give big advantage
                addSkills(cardDTO.getSkills(), "active");
                break;
            default:
                // Handle unexpected rarities
                break;
        }
    }

    private void applyAttributes(Map<String, Integer> attributes, List<String> attributeChanges) {
        if (attributes != null) {
            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                String attribute = entry.getKey();
                Integer value = entry.getValue();

                switch (attribute.toLowerCase()) {
                    case "hp":
                        this.maxHp += value;
                        this.hp += value;
                        // Ensure hp does not exceed maxHp
                        if (this.hp > this.maxHp) {
                            this.hp = this.maxHp;
                        }
                        attributeChanges.add("+" + value + " HP");
                        break;
                    case "attack":
                        this.attack += value;
                        attributeChanges.add("+" + value + " Attack");
                        break;
                    case "defense":
                        this.defense += value;
                        attributeChanges.add("+" + value + " Defense");
                        break;
                    case "attackdamage":
                        this.attackDamage += value;
                        attributeChanges.add("+" + value + " Attack Damage");
                        break;
                    case "attackspeed":
                        this.attackSpeed += value;
                        attributeChanges.add("+" + value + " Attack Speed");
                        break;
                    case "mana":
                        this.mana += value;
                        if (this.mana > this.maxMana) {
                            this.mana = this.maxMana;
                        }
                        attributeChanges.add("+" + value + " Mana");
                        break;
                    case "manacost":
                        this.mana -= value;
                        attributeChanges.add("-" + value + " Mana");
                        break;
                    default:
                        // Handle other attributes
                        break;
                }
            }
        }
    }

    private void addSkills(List<SkillDTO> skillDTOs, String expectedType) {
        if (skillDTOs != null) {
            for (SkillDTO skillDTO : skillDTOs) {
                if (expectedType.equalsIgnoreCase(skillDTO.getType())) {
                    Skill skill = new Skill();
                    skill.setName(skillDTO.getName());
                    skill.setManaCost(skillDTO.getManaCost());
                    skill.setDamage(skillDTO.getDamage());
                    skill.setCooldown(skillDTO.getCooldown());
                    skill.setLastUsedRound(-skill.getCooldown());
                    skill.setType(skillDTO.getType());
                    skill.setEffect(skillDTO.getEffect());
                    skill.setValue(skillDTO.getValue());
                    this.skills.add(skill);
                }
            }
        }
    }
}