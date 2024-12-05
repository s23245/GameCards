
package com.example.gamecards.services;

import com.example.gamecards.DTO.*;
import com.example.gamecards.controller.DuelController;
import com.example.gamecards.models.*;
import com.example.gamecards.repositories.CardRepository;
import com.example.gamecards.repositories.GameSessionRepository;
import com.example.gamecards.repositories.HeroRepository;
import com.example.gamecards.repositories.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DuelService {
    private static final Logger logger = LoggerFactory.getLogger(DuelService.class);

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private HeroRepository heroRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Stores players' chosen cards temporarily
    private final Map<UUID, Map<String, CardDTO>> playerChosenCardsPerGame = new ConcurrentHashMap<>();

    private final Map<UUID, Set<String>> connectedPlayers = new ConcurrentHashMap<>();

    public void addConnectedPlayer(UUID gameId, String username) {
        connectedPlayers.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public void removeConnectedPlayers(UUID gameId) {
        connectedPlayers.remove(gameId);
    }

    public Set<String> getConnectedPlayers(UUID gameId) {
        return connectedPlayers.get(gameId);
    }

    @Async("taskExecutor")
    public void startGameSession(UUID gameId) {
        logger.info("GameID:{}", gameId);
        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));

        synchronized (gameSession) {
            if (gameSession.isDuelStarted()) {
                logger.info("Duel already started for gameId: {}", gameId);
                return;
            }
            gameSession.setDuelStarted(true);
            gameSessionRepository.save(gameSession);
        }

        try {
            startGameLoop(gameId);
        } catch (Exception e) {
            logger.error("Exception during game loop", e);
            messagingTemplate.convertAndSend("/topic/game-error/" + gameId, "Game session failed: " + e.getMessage());
        }
    }

    protected void startGameLoop(UUID gameId) throws InterruptedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("Authenticated user in startGameLoop: {}", auth.getName());
        } else {
            logger.warn("No authenticated user in startGameLoop");
        }

        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));
        logger.info("Starting game loop in session: {}", gameSession);

        // Initialize players list if empty
        if (gameSession.getPlayers() == null || gameSession.getPlayers().isEmpty()) {
            List<Player> players = new ArrayList<>();
            for (String username : gameSession.getUsers()) {
                Player player = new Player();
                player.setUsername(username);
                player.setHp(10);
                players.add(player);
                logger.info("Created player: {}", player);
            }
            gameSession.setPlayers(players);
        }

        // Initialize heroes for players with null heroes
        for (Player player : gameSession.getPlayers()) {
            if (player.getHero() == null) {
                Long heroId = gameSession.getSelectedHeroes().get(player.getUsername());
                if (heroId == null) {
                    throw new IllegalArgumentException("No hero selected for player " + player.getUsername());
                }
                Hero hero = createHeroFromTemplate(heroId);
                player.setHero(hero);
                logger.info("Initialized hero for player: {}", player);
            }
        }

        gameSessionRepository.save(gameSession);

        // Send initial players' status
        sendPlayersStatus(gameSession);

        // Main game loop
        int roundNumber = 1;
        boolean gameActive = true;
        while (gameActive) {
            // Base stage
            for (Player player : gameSession.getPlayers()) {
                logger.info("Hero stats sent to player: {}", player);
                // Send hero stats to player
                sendHeroStats(player, gameId);

                // Generate and send 3 random cards
                List<Card> cards = generateRandomCards(3, roundNumber);
                logger.info("Cards :{}", cards);
                sendCardsToPlayer(player, cards, gameId);
            }

            logger.info("Waiting for players to choose cards");
            waitForPlayersToChooseCards(gameSession);
            logger.info("All players have chosen their cards");

            // Apply chosen cards to heroes
            logger.info("Applying chosen cards to heroes");
            applyChosenCards(gameSession);
            logger.info("Applied chosen cards");

            // Start duel after countdown
            logger.info("Sending countdown");
            messagingTemplate.convertAndSend("/topic/countdown/" + gameId, "5");
            Thread.sleep(5000);

            // Run duel
            logger.info("Running duel");
            runDuel(gameSession);
            logger.info("Duel completed");

            // Check if any player has 0 HP
            logger.info("Checking players' status");
            gameActive = checkPlayersStatus(gameSession);
            logger.info("Game active: {}", gameActive);

            sendPlayersStatus(gameSession);

            // Save game session
            gameSessionRepository.save(gameSession);

            // Increment round number
            roundNumber++;
        }

        // End game session
        messagingTemplate.convertAndSend("/topic/game-over/" + gameId, "Game Over");
        playerChosenCardsPerGame.remove(gameId);
        connectedPlayers.remove(gameId); // Clear connected players
        gameSession.setDuelStarted(false);
        gameSessionRepository.save(gameSession);
    }

    private void sendHeroStats(Player player, UUID gameId) {
        try {
            Hero hero = player.getHero();
            HeroDTO heroDTO = new HeroDTO();

            heroDTO.setName(hero.getName());
            heroDTO.setHp(hero.getHp());
            heroDTO.setMaxHp(hero.getMaxHp());
            heroDTO.setMana(hero.getMana());
            heroDTO.setMaxMana(hero.getMaxMana());
            heroDTO.setAttack(hero.getAttack());
            heroDTO.setDefense(hero.getDefense());
            heroDTO.setAttackDamage(hero.getAttackDamage());
            heroDTO.setAttackSpeed(hero.getAttackSpeed());
            heroDTO.setMainElement(hero.getMainElement());
            heroDTO.setImageUrl(hero.getImageUrl());

            HeroUpdate heroUpdate = new HeroUpdate(heroDTO, new ArrayList<>());

            // Log the heroUpdate object
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(heroUpdate);
            logger.info("Sending heroUpdate to player {}: {}", player.getUsername(), json);

            messagingTemplate.convertAndSend(
                    "/topic/hero-stats/" + gameId + "/" + player.getUsername(),
                    heroUpdate
            );
            logger.info("Sending hero stats to player: {}", player.getUsername());
        } catch (Exception e) {
            logger.error("Error sending hero stats to player {}:", player.getUsername(), e);
        }
    }

    private Map<String, Integer> getRarityProbabilities(int round) {
        Map<String, Integer> probabilities = new HashMap<>();

        probabilities.put("common", 50);
        probabilities.put("uncommon", 30);
        probabilities.put("rare", 15);
        probabilities.put("epic", 4);
        probabilities.put("legendary", 1);

        if (round > 3) {
            probabilities.put("common", 40);
            probabilities.put("uncommon", 30);
            probabilities.put("rare", 20);
            probabilities.put("epic", 8);
            probabilities.put("legendary", 2);
        }
        if (round > 5) {
            probabilities.put("common", 30);
            probabilities.put("uncommon", 30);
            probabilities.put("rare", 25);
            probabilities.put("epic", 10);
            probabilities.put("legendary", 5);
        }
        return probabilities;
    }

    private List<Card> generateRandomCards(int numberOfCards, int round) {
        Map<String, Integer> rarityProbabilities = getRarityProbabilities(round);
        List<Card> selectedCards = new ArrayList<>();

        List<Card> allCards = cardRepository.findAll();
        Map<String, List<Card>> cardsByRarity = allCards.stream()
                .collect(Collectors.groupingBy(card -> card.getRarity().toLowerCase()));

        Random rand = new Random();
        for (int i = 0; i < numberOfCards; i++) {
            int randomNum = rand.nextInt(100) + 1; // 1 to 100
            String selectedRarity = null;
            int cumulativeProbability = 0;

            for (Map.Entry<String, Integer> entry : rarityProbabilities.entrySet()) {
                cumulativeProbability += entry.getValue();
                if (randomNum <= cumulativeProbability) {
                    selectedRarity = entry.getKey();
                    break;
                }
            }

            List<Card> rarityCards = cardsByRarity.get(selectedRarity);
            if (rarityCards != null && !rarityCards.isEmpty()) {
                Card card = rarityCards.get(rand.nextInt(rarityCards.size()));
                selectedCards.add(card);
            } else {
                // Fallback to common if no cards of the selected rarity
                List<Card> commonCards = cardsByRarity.get("common");
                if (commonCards != null && !commonCards.isEmpty()) {
                    Card card = commonCards.get(rand.nextInt(commonCards.size()));
                    selectedCards.add(card);
                }
            }
        }

        return selectedCards;
    }

    private void sendCardsToPlayer(Player player, List<Card> cards, UUID gameId) {
        try {
            List<CardDTO> cardDTOs = cards.stream().map(card -> {
                CardDTO dto = new CardDTO();
                dto.setId(card.getId());
                dto.setName(card.getName());
                dto.setDescription(card.getDescription());
                dto.setRarity(card.getRarity());
                dto.setImageUrl(card.getImageUrl());
                dto.setAttributes(new HashMap<>(card.getAttributes()));

                // Map skills to SkillDTOs
                if (card.getSkills() != null) {
                    List<SkillDTO> skillDTOs = card.getSkills().stream().map(skill -> {
                        SkillDTO skillDTO = new SkillDTO();
                        skillDTO.setId(skill.getId());
                        skillDTO.setName(skill.getName());
                        skillDTO.setManaCost(skill.getManaCost());
                        skillDTO.setDamage(skill.getDamage());
                        skillDTO.setCooldown(skill.getCooldown());
                        skillDTO.setType(skill.getType());
                        skillDTO.setEffect(skill.getEffect());
                        skillDTO.setValue(skill.getValue());
                        return skillDTO;
                    }).collect(Collectors.toList());
                    dto.setSkills(skillDTOs);
                }

                return dto;
            }).collect(Collectors.toList());

            messagingTemplate.convertAndSend("/topic/cards/" + gameId + "/" + player.getUsername(), cardDTOs);
        } catch (Exception e) {
            logger.error("Error sending cards to player {}:", player.getUsername(), e);
        }
    }

    private void waitForPlayersToChooseCards(GameSession gameSession) throws InterruptedException {
        UUID gameId = gameSession.getId();
        Map<String, CardDTO> chosenCards = playerChosenCardsPerGame.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>());

        int maxWaitTimeSeconds = 30; // Maximum time to wait
        int waitedSeconds = 0;

        while (chosenCards.size() < gameSession.getPlayers().size()) {
            logger.info("Waiting for players to choose cards: {}/{}", chosenCards.size(), gameSession.getPlayers().size());
            Thread.sleep(1000);
            waitedSeconds++;

            if (waitedSeconds >= maxWaitTimeSeconds) {
                logger.warn("Timeout waiting for players to choose cards");
                break;
            }
        }
        logger.info("All players have chosen their cards or timeout reached.");
    }

    private void applyChosenCards(GameSession gameSession) {
        UUID gameId = gameSession.getId();
        Map<String, CardDTO> chosenCards = playerChosenCardsPerGame.get(gameId);

        if (chosenCards != null) {
            for (Player player : gameSession.getPlayers()) {
                CardDTO chosenCard = chosenCards.get(player.getUsername());
                if (chosenCard != null) {
                    // Collect attribute changes
                    List<String> attributeChanges = new ArrayList<>();
                    // attributeChanges should be sent here

                    // Apply card and collect attribute changes
                    player.getHero().applyCard(chosenCard, attributeChanges);

                    // Send updated hero stats and attribute changes to player
                    Hero hero = player.getHero();
                    HeroDTO heroDTO = new HeroDTO();
                    heroDTO.setName(hero.getName());
                    heroDTO.setHp(hero.getHp());
                    heroDTO.setMaxHp(hero.getMaxHp());
                    heroDTO.setMana(hero.getMana());
                    heroDTO.setMaxMana(hero.getMaxMana());
                    heroDTO.setAttack(hero.getAttack());
                    heroDTO.setDefense(hero.getDefense());
                    heroDTO.setAttackDamage(hero.getAttackDamage());
                    heroDTO.setAttackSpeed(hero.getAttackSpeed());
                    heroDTO.setMainElement(hero.getMainElement());
                    heroDTO.setImageUrl(hero.getImageUrl());

                    HeroUpdate heroUpdate = new HeroUpdate(heroDTO, attributeChanges);

                    messagingTemplate.convertAndSend(
                            "/topic/hero-stats/" + gameId + "/" + player.getUsername(),
                            heroUpdate
                    );
                } else {
                    logger.warn("No card chosen by player {}", player.getUsername());
                }
            }
            // Clear the map for the next round
            chosenCards.clear();
        } else {
            logger.warn("No chosen cards found for game {}", gameId);
        }
    }

    private void runDuel(GameSession gameSession) throws InterruptedException {
        if (gameSession.getPlayers().size() != 2) {
            throw new IllegalStateException("Exactly two players are required for the duel.");
        }

        Player player1 = gameSession.getPlayers().get(0);
        Player player2 = gameSession.getPlayers().get(1);

        Hero hero1 = player1.getHero();
        Hero hero2 = player2.getHero();

        // Log hero stats before the duel
        logger.info("Before Duel - {}'s Hero HP: {}, MaxHP: {}", player1.getUsername(), hero1.getHp(), hero1.getMaxHp());
        logger.info("Before Duel - {}'s Hero HP: {}, MaxHP: {}", player2.getUsername(), hero2.getHp(), hero2.getMaxHp());

        // Simulate duel in rounds
        simulateDuel(hero1, hero2, gameSession);

        // Determine winner and update players' HP
        if (hero1.getHp() > 0) {
            // Player 2 loses HP
            player2.setHp(player2.getHp() - 1);
            messagingTemplate.convertAndSend("/topic/duel-result/" + gameSession.getId(), player1.getUsername() + " wins the duel!");
        } else if (hero2.getHp() > 0) {
            // Player 1 loses HP
            player1.setHp(player1.getHp() - 1);
            messagingTemplate.convertAndSend("/topic/duel-result/" + gameSession.getId(), player2.getUsername() + " wins the duel!");
        } else {
            // Both heroes are defeated; it's a tie
            player1.setHp(player1.getHp() - 1);
            player2.setHp(player2.getHp() - 1);
            messagingTemplate.convertAndSend("/topic/duel-result/" + gameSession.getId(), "It's a tie!");
        }

        // Reset heroes' HP for next round
        hero1.setHp(hero1.getMaxHp());
        hero2.setHp(hero2.getMaxHp());

        sendPlayersStatus(gameSession);
    }

    private void simulateDuel(Hero hero1, Hero hero2, GameSession gameSession) throws InterruptedException {
        int round = 1;
        UUID gameId = gameSession.getId();

        while (hero1.getHp() > 0 && hero2.getHp() > 0) {
            List<String> logs = new ArrayList<>();
            List<String> hero1AttributeChanges = new ArrayList<>();
            List<String> hero2AttributeChanges = new ArrayList<>();

            // Process passive skills
            processPassiveSkills(hero1, logs);
            processPassiveSkills(hero2, logs);

            // Hero1 uses active skills
            useActiveSkills(hero1, hero2, round, logs);

            // Hero2 uses active skills
            useActiveSkills(hero2, hero1, round, logs);

            // Regular attacks
            int hero1Damage = calculateDamage(hero1.getAttackDamage(), hero2.getDefense());
            int hero2Damage = calculateDamage(hero2.getAttackDamage(), hero1.getDefense());

            // Apply damage
            hero2.setHp(hero2.getHp() - hero1Damage);
            hero1.setHp(hero1.getHp() - hero2Damage);

            // Ensure HP doesn't go below zero
            hero1.setHp(Math.max(hero1.getHp(), 0));
            hero2.setHp(Math.max(hero2.getHp(), 0));

            // Log attacks
            logs.add(hero1.getName() + " attacks " + hero2.getName() + " for " + hero1Damage + " damage.");
            logs.add(hero2.getName() + " attacks " + hero1.getName() + " for " + hero2Damage + " damage.");

            // Send duel updates to clients
            DuelUpdate duelUpdate = new DuelUpdate(
                    hero1.getName(), hero1,
                    hero2.getName(), hero2,
                    logs
            );

            messagingTemplate.convertAndSend("/topic/duel-progress/" + gameId, duelUpdate);

            // Wait before next round
            Thread.sleep(1000);

            round++;
        }
    }

    private int calculateDamage(int attackDamage, int defense) {
        int damage = attackDamage - (defense / 2);
        return Math.max(damage, 0);
    }

    private void processPassiveSkills(Hero hero, List<String> logs) {
        for (Skill skill : hero.getSkills()) {
            if ("passive".equalsIgnoreCase(skill.getType())) {
                switch (skill.getEffect()) {
                    case "hp_regen":
                        int hpBefore = hero.getHp();
                        hero.setHp(Math.min(hero.getHp() + skill.getValue(), hero.getMaxHp()));
                        int hpGained = hero.getHp() - hpBefore;
                        if (hpGained > 0) {
                            logs.add(hero.getName() + " regenerates " + hpGained + " HP.");
                        }
                        break;
                    case "mana_regen":
                        int manaBefore = hero.getMana();
                        hero.setMana(Math.min(hero.getMana() + skill.getValue(), hero.getMaxMana()));
                        int manaGained = hero.getMana() - manaBefore;
                        if (manaGained > 0) {
                            logs.add(hero.getName() + " regenerates " + manaGained + " Mana.");
                        }
                        break;
                    // Add other passive effects as needed
                }
            }
        }
    }

    private void useActiveSkills(Hero attacker, Hero defender, int currentRound, List<String> logs) {
        for (Skill skill : attacker.getSkills()) {
            if ("active".equalsIgnoreCase(skill.getType())) {
                // Check cooldown
                if (currentRound - skill.getLastUsedRound() >= skill.getCooldown()) {
                    // Check mana cost
                    if (attacker.getMana() >= skill.getManaCost()) {
                        // Use the skill
                        attacker.setMana(attacker.getMana() - skill.getManaCost());
                        skill.setLastUsedRound(currentRound);

                        // Apply effect
                        switch (skill.getEffect()) {
                            case "damage":
                                defender.setHp(defender.getHp() - skill.getValue());
                                logs.add(attacker.getName() + " uses " + skill.getName() + " dealing " + skill.getValue() + " damage to " + defender.getName() + ".");
                                break;
                            case "heal":
                                int hpBefore = attacker.getHp();
                                attacker.setHp(Math.min(attacker.getHp() + skill.getValue(), attacker.getMaxHp()));
                                int hpGained = attacker.getHp() - hpBefore;
                                logs.add(attacker.getName() + " uses " + skill.getName() + " and heals for " + hpGained + " HP.");
                                break;
                            // Add other active effects as needed
                        }

                        // Ensure defender's HP doesn't go below zero
                        defender.setHp(Math.max(defender.getHp(), 0));
                    }
                }
            }
        }
    }

    private boolean checkPlayersStatus(GameSession gameSession) {
        for (Player player : gameSession.getPlayers()) {
            if (player.getHp() <= 0) {
                messagingTemplate.convertAndSend("/topic/game-over/" + gameSession.getId(), player.getUsername() + " has been defeated!");
                return false;
            }
        }
        return true;
    }

    @Transactional
    public Map<String, Object> applyChosenCard(Long cardId, UUID gameId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("applyChosenCard called with username={}, cardId={}, gameId={}", username, cardId, gameId);

        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));
        Player player = gameSession.getPlayer(username);
        if (player == null) {
            throw new IllegalArgumentException("Player not found in game session");
        }
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        // Initialize collections
        card.getAttributes().size();
        card.getSkills().size();

        // Convert to DTO
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(card.getId());
        cardDTO.setName(card.getName());
        cardDTO.setDescription(card.getDescription());
        cardDTO.setRarity(card.getRarity());
        cardDTO.setImageUrl(card.getImageUrl());
        cardDTO.setAttributes(new HashMap<>(card.getAttributes()));

        if (card.getSkills() != null) {
            List<SkillDTO> skillDTOs = card.getSkills().stream().map(skill -> {
                SkillDTO skillDTO = new SkillDTO();
                skillDTO.setId(skill.getId());
                skillDTO.setName(skill.getName());
                skillDTO.setManaCost(skill.getManaCost());
                skillDTO.setDamage(skill.getDamage());
                skillDTO.setCooldown(skill.getCooldown());
                skillDTO.setType(skill.getType());
                skillDTO.setEffect(skill.getEffect());
                skillDTO.setValue(skill.getValue());
                return skillDTO;
            }).collect(Collectors.toList());
            cardDTO.setSkills(skillDTOs);
        }

        // Store the DTO
        playerChosenCardsPerGame
                .computeIfAbsent(gameId, k -> new ConcurrentHashMap<>())
                .put(username, cardDTO);

        logger.info("Player {} has chosen card {}", username, cardDTO);

        // Simulate applying the card to get attribute changes
        List<String> attributeChanges = simulateAttributeChanges(player.getHero(), card);

        // Return attribute changes to the client
        Map<String, Object> response = new HashMap<>();
        response.put("attributeChanges", attributeChanges);

        logger.info("Player {} has chosen card {}", username, cardDTO);
        return response;
    }
    private List<String> simulateAttributeChanges(Hero hero, Card card) {
        List<String> attributeChanges = new ArrayList<>();
        Map<String, Integer> attributes = card.getAttributes();

        if (attributes != null) {
            attributes.forEach((key, value) -> {
                attributeChanges.add(key.toUpperCase() + ": " + (value > 0 ? "+" : "") + value);
            });
        }

        if (card.getSkills() != null) {
            for (Skill skill : card.getSkills()) {
                attributeChanges.add("Skill: " + skill.getName() + " (" + skill.getEffect() + ": " + skill.getValue() + ")");
            }
        }
        return attributeChanges;
    }

    private Hero createHeroFromTemplate(Long heroId) {
        Hero selectedHeroTemplate = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Hero not found"));

        Hero hero = new Hero();
        hero.setName(selectedHeroTemplate.getName());
        hero.setHp(selectedHeroTemplate.getHp());
        hero.setMaxHp(selectedHeroTemplate.getMaxHp());
        hero.setMana(selectedHeroTemplate.getMana());
        hero.setMaxMana(selectedHeroTemplate.getMaxMana());
        hero.setAttack(selectedHeroTemplate.getAttack());
        hero.setDefense(selectedHeroTemplate.getDefense());
        hero.setAttackDamage(selectedHeroTemplate.getAttackDamage());
        hero.setAttackSpeed(selectedHeroTemplate.getAttackSpeed());
        hero.setMainElement(selectedHeroTemplate.getMainElement());
        hero.setImageUrl(selectedHeroTemplate.getImageUrl());

        // Add default values if necessary
        if (hero.getAttackDamage() <= 0) {
            hero.setAttackDamage(10); // Set a default value
        }
        if (hero.getAttackSpeed() <= 0) {
            hero.setAttackSpeed(1); // Set a default value
        }

        return hero;
    }

    private void sendPlayersStatus(GameSession gameSession) {
        List<PlayerStatus> playerStatuses = gameSession.getPlayers().stream()
                .map(player -> new PlayerStatus(player.getUsername(), player.getHp()))
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/players-status/" + gameSession.getId(), playerStatuses);
    }
}