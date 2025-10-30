package com.example.memorialparkpart1.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtBean {

    @Bean
    public JwtUtil jwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-minutes:60}") long expiryMinutes
    ) {
        // JwtUtil 생성자가 minutes를 받아서 내부에서 * 60_000 하도록 되어 있음
        return new JwtUtil(secret, expiryMinutes);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthFilter(jwtUtil);
    }

}