package com.example.gamecards.services;

import com.example.gamecards.DTO.UserDTO;
import com.example.gamecards.models.User;

public interface UserService
{
    User findByUsername(String username);
    User save(UserDTO userDTO);
}
