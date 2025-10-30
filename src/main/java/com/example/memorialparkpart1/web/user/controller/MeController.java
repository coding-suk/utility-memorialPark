package com.example.memorialparkpart1.web.user.controller;

import com.example.memorialparkpart1.common.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class MeController {

    private final JwtUtil jwt;
    public MeController(JwtUtil jwt) {
        this.jwt = jwt;
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {

        String token = null;

        Cookie[] cs =  req.getCookies();
        if(cs != null) {
            for(Cookie c : req.getCookies()) {
                if("AUTH".equals(c.getName())) {
                    token = c.getValue();
                }
            }
        }
        if(token == null) {
            return ResponseEntity.status(401).build();
        }

        Jws<Claims> jws = jwt.parse(token);

        var body = jws.getBody();
        return ResponseEntity.ok(Map.of(
                "id", body.getSubject(),
                "name", body.get("name", String.class),
                "role", body.get("role", String.class)
        ));
    }
}

