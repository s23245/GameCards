package com.example.gamecards;

import com.example.gamecards.models.User;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableAsync
@SpringBootApplication
public class GameCardsApplication {
    private static UserRepository userRepository;
    public static void main(String[] args)
    {
        SpringApplication.run(GameCardsApplication.class, args);
    }

}
