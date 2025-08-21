package com.epam.hw.feign;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {


    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String jwtToken = authHeader.substring(7);
                    if (requestTemplate.body() != null) {
                        log.info("Request body: {}", new String(requestTemplate.body()));
                    }
                    requestTemplate.header("Authorization", "Bearer " + jwtToken);
                    log.info("Added JWT token to Feign request: {}", jwtToken);
                } else {
                    log.warn("No Bearer token found in request header");
                }
            } else {
                log.warn("No request attributes available for Feign request");
            }
        };
    }
}
