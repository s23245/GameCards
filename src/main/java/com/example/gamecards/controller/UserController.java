package com.example.gamecards.controller;

import com.example.gamecards.models.User;
import com.example.gamecards.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/current")
    public User getCurrentUser(Authentication authentication)
    {
        Optional<User> user = userService.findByUsername(authentication.getName());

        if(user.isEmpty())
            throw new UsernameNotFoundException("User not found");
        return user.get();
    }

    @PutMapping("/username")
    public User updateUsername(Authentication authentication, @RequestBody Map<String, String> body) {
        String email = authentication.getName();
        String username = body.get("username");
        return userService.updateUsername(email, username);
    }
}
