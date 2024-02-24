package com.example.gamecards.services;

import com.example.gamecards.DTO.UserDTO;
import com.example.gamecards.models.User;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceImpl implements UserService
{
    @Autowired
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }
    @Override
    public User findByUsername(String username)
    {
        return userRepository.findByEmail(username);
    }

    @Override
    public User save(UserDTO userDTO)
    {
        User user = new User(userDTO.getEmail(),passwordEncoder.encode(userDTO.getPassword()), userDTO.getFullname());
        return userRepository.save(user);
    }
}
