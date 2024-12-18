package com.example.gamecards.controller;

import com.example.gamecards.DTO.LoginRequest;
import com.example.gamecards.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import com.example.gamecards.services.CustomUserDetailService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            final UserDetails userDetails = customUserDetailService.loadUserByUsername(loginRequest.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails.getUsername());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Authorization", "Bearer " + token);

            return ResponseEntity.ok().headers(responseHeaders).body(Map.of("token", token,"username", customUserDetailService.loadUserByUsername(loginRequest.getUsername()).getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }

}
