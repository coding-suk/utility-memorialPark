package com.example.memorialparkpart1.web.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    //접근 확인
    @GetMapping("/ping")
    public Map<String, Object> ping(Authentication auth) {
        return Map.of(
                "ok", true,
                "who", auth.getName(), // JwtAuthFilter에서 넣은 principal(userId)
                "authorities", auth.getAuthorities().toString()
        );
    }

    // 샘플 사용자 목록(추후 DB로 교체)
    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return List.of(
                Map.of("id", "fake-1001", "name", "테스트사용자", "role", "USER"),
                Map.of("id", "admin-1",   "name", "관리자",     "role", "ADMIN")
        );
    }

}
