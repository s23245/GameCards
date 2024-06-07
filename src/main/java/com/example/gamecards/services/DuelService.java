package com.example.gamecards.services;

import com.example.gamecards.models.Hero;
import com.example.gamecards.DTO.DuelUpdate;
import com.example.gamecards.repositories.HeroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class DuelService {

    @Autowired
    private HeroRepository heroRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    public void startDuel(Long hero1Id, Long hero2Id) throws Exception
    {
        Optional<Hero> hero1Opt = heroRepository.findById(hero1Id);
        Optional<Hero> hero2Opt = heroRepository.findById(hero2Id);

        if (hero1Opt.isEmpty() || hero2Opt.isEmpty()) {
            throw new Exception("One or both heroes not found");
        }

        Hero hero1 = hero1Opt.get();
        Hero hero2 = hero2Opt.get();

        while (hero1.getHp() > 0 && hero2.getHp() > 0) {
            performAttack(hero1, hero2);
            messagingTemplate.convertAndSend("/topic/duel-progress", new DuelUpdate(hero1, hero2));
            if (hero2.getHp() <= 0) break; // Stop if Hero 2 is defeated

            performAttack(hero2, hero1);
            messagingTemplate.convertAndSend("/topic/duel-progress", new DuelUpdate(hero1, hero2));
            if (hero1.getHp() <= 0) break; // Stop if Hero 1 is defeated

            Thread.sleep(1000 / Math.max(hero1.getAttackSpeed(), hero2.getAttackSpeed()));
        }

        if (hero1.getHp() > 0) {
            messagingTemplate.convertAndSend("/topic/duel-result", "Hero 1 wins!");
        } else {
            messagingTemplate.convertAndSend("/topic/duel-result", "Hero 2 wins!");
        }

    }

    private void performAttack(Hero attacker, Hero defender) {
        int baseDamage = attacker.getAttackDamage();
        int randomFactor = random.nextInt(21) - 10; // Random factor between -10 and 10
        int damage = baseDamage + randomFactor - defender.getDefense();
        if (damage < 0) damage = 0;

        defender.setHp(defender.getHp() - damage);

        attacker.setMana(attacker.getMana() - 10);
        if (attacker.getMana() < 0) attacker.setMana(0);
    }
}
