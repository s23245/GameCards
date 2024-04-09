package com.example.gamecards.controller;

import com.example.gamecards.DTO.RegistrationRequest;
import com.example.gamecards.models.User;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController
{
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegistrationRequest request)
    {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User req = userRepository.save(user);
        if(req.getUserId() > 0)
            return ResponseEntity.ok("User registered successfully");
        return ResponseEntity.badRequest().body("User registration failed");
    }
    @GetMapping("/users/get/all")
    public ResponseEntity<Object> getAllUsers()
    {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
