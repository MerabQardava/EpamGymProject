package com.epam.WorkloadMicroservice.messaging;

import com.epam.WorkloadMicroservice.security.JWTService;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class messageConsumer {
    private final JWTService jwtService;

    @Autowired
    public messageConsumer(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @JmsListener(destination = "workload.queue")
    public void handleMessage(String message, @Header("Authorization") String jwt) {


        if (jwt != null && jwt.startsWith("Bearer ")) {
            String token = jwt.substring(7);
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUserName(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                new User(username, "", Collections.emptyList()),
                                null,
                                Collections.emptyList()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        System.out.println("Received message: " + message);
        System.out.println("Received token: " + jwt);
    }

}
