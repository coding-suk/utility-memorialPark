package com.example.memorialparkpart1.web.user.controller;

import com.example.memorialparkpart1.common.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class FakeAuthController {

    private final JwtUtil jwt;
    public FakeAuthController(JwtUtil jwt) {
        this.jwt = jwt;
    }

    @GetMapping("/login/fake")
    public void loginFake(HttpServletResponse res, HttpSession session) throws Exception{
        String state = UUID.randomUUID().toString();
        session.setAttribute("STATE", state);

        // 카카오를 흉내내서 곧바로 콜백으로 돌려보냄
        res.sendRedirect("/auth/callback/fake?code=abc123&state=" + state);
    }

    @GetMapping("/callback/fake")
    public ResponseEntity<Void> callbackFake(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session,
            HttpServletResponse res) {
        String saved = (String) session.getAttribute("STATE");

        if(saved == null || !saved.equals(state)) {
            return ResponseEntity.badRequest().build();
        }

        String token = jwt.createToken("fake-1001", "테스트 사용자", "USER");

        var c = new jakarta.servlet.http.Cookie("AUTH", token);
        c.setHttpOnly(true);
        c.setPath("/");
        c.setSecure(true); // HTTPS로 사용하지 않는다면 비활성화
        res.addCookie(c);

        return ResponseEntity.status(302).location(URI.create("/mypage.html")).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {

        var c = new jakarta.servlet.http.Cookie("AUTH", "");
        c.setPath("/");
        c.setMaxAge(0);
        res.addCookie(c);
        return ResponseEntity.noContent().build();

    }


}