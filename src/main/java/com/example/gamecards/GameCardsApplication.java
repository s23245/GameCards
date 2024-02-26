package com.example.gamecards;

import com.example.gamecards.services.CustomUserDetails;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

@SpringBootApplication
public class GameCardsApplication {

    public static void main(String[] args)
    {

        SpringApplication.run(GameCardsApplication.class, args);
    }

}
