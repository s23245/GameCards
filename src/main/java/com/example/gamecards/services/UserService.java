package com.example.gamecards.services;

import com.example.gamecards.DTO.RegistrationRequest;
import com.example.gamecards.models.Role;
import com.example.gamecards.models.User;
import com.example.gamecards.repositories.RoleRepository;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class UserService
{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;



    public String registerUser(RegistrationRequest request)
    {
        System.out.println("First Name: " + request.getFirstName());
        System.out.println("Last Name: " + request.getLastName());

        if (userRepository.existsByEmail(request.getEmail())) {
            return "User with this email already exists";
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findByName("ROLE_USER");
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        User savedUser = userRepository.save(user);
        if (savedUser.getUserId() > 0) {
            return "User registered successfully";
        } else {
            return "User registration failed";
        }
    }
}
