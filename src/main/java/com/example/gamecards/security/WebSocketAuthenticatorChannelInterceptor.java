package com.example.gamecards.security;

import com.example.gamecards.services.CustomUserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebSocketAuthenticatorChannelInterceptor implements ChannelInterceptor
{
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthenticatorChannelInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                // Try to get the token from the session attributes
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                String tokenFromSession = (String) sessionAttributes.get("token");
                if (tokenFromSession != null) {
                    token = "Bearer " + tokenFromSession;
                }
            }

            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String username = jwtUtil.extractUsername(jwt);
                logger.info("Extracted Username from WebSocket: {}", username);

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                        logger.info("User authenticated in WebSocket: {}", username);
                    } else {
                        logger.warn("Invalid JWT token in WebSocket connection");
                    }
                } else {
                    logger.warn("Username extracted from JWT is null in WebSocket connection");
                }
            } else {
                logger.warn("JWT Token is missing or does not start with Bearer String in WebSocket");
            }
        }

        return message;
    }
}
