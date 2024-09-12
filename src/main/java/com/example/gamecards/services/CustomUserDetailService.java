package com.example.gamecards.services;

import com.example.gamecards.models.Role;
import com.example.gamecards.models.User;
import com.example.gamecards.repositories.RoleRepository;
import com.example.gamecards.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }

    public Long getUserIdByEmail(String email)
    {
        User user = userRepository.findByEmail(email);
        if(user == null)
            throw new UsernameNotFoundException("No user found by such email");
        return user.getUserId();
    }

    public User getUserByEmail(String email)
    {
        User user = userRepository.findByEmail(email);
        if(user == null)
             throw new UsernameNotFoundException("No user found by such email");
        return user;
    }

}
