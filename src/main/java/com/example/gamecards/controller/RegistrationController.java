package com.example.gamecards.controller;

import com.example.gamecards.DTO.RegistrationRequest;
import com.example.gamecards.repositories.UserRepository;
import com.example.gamecards.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin(origins = "http://localhost:5173")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        String result = userService.registerUser(request);
        if (result.equals("User registered successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/users/get/all")
    public ResponseEntity<Object> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/home")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to the Game Cards App");
    }
}
