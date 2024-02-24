package com.example.gamecards.services;

import com.example.gamecards.models.User;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Service
public class CustomerUserDetailsService implements UserDetailsService
{

    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByEmail(username);
        if(user == null)
            throw new UsernameNotFoundException("USER IS NOT FOUND ");

        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                authorities(),
                user.getFullname());
    }

    public Collection<? extends GrantedAuthority> authorities()
    {
        return Arrays.asList(new SimpleGrantedAuthority("USER"));
    }

}
