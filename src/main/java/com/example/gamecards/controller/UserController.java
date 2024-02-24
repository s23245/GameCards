package com.example.gamecards.controller;

import com.example.gamecards.DTO.UserDTO;
import com.example.gamecards.models.User;
import com.example.gamecards.services.UserService;
import com.example.gamecards.services.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController
{
    @Autowired
    private UserDetailsService userDetailsService;
    private UserService userService;

    @GetMapping("/home")
    public String home(Model model, Principal principal)
    {
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("userdetail",userDetails);
        
        return "home";
    }
    @GetMapping("/login")
    public String login(Model model, UserDTO userDTO)
    {
        model.addAttribute("user",userDTO);
        return "login";
    }
    @GetMapping("/register")
    public ResponseEntity<?> registerUser(Model model, UserDTO userDTO)
    {
        model.addAttribute("user",userDTO);
        return ResponseEntity.ok("User registered succesfully");
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerAndSaveUser(@ModelAttribute("user")UserDTO userDTO)
    {
        userService.save(userDTO);
        return ResponseEntity.ok("User registered succesfully");
    }
}
