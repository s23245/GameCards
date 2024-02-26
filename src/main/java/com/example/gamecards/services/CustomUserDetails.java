package com.example.gamecards.services;

import com.example.gamecards.models.User;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Service
public class CustomUserDetails implements UserDetails
{
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private String fullname;
    private final UserRepository userRepository;
    public CustomUserDetails() {
        this.userRepository = null;
    }

    public CustomUserDetails(String email, String password, Collection<? extends GrantedAuthority> authorities,
                             String fullname,UserRepository userRepository)
    {
        this.username = email;
        this.password = password;
        this.authorities = authorities;
        this.fullname = fullname;
        this.userRepository = userRepository;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }


    public String getFullname() {
        return fullname;
    }
}
