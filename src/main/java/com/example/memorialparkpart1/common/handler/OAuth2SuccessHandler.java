package com.example.memorialparkpart1.common.handler;

import com.example.memorialparkpart1.common.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final Environment env;

    @Value("${app.oauth2.redirect-uri:https://frontend.example.com/oauth2/success}")
    private String redirectBase;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {

        var principal = (DefaultOAuth2User) authentication.getPrincipal();

        String userId = (String) principal.getAttributes().get("userId"); // string으로 꺼냄
        String name = (String) principal.getAttributes().get("name");
        String role = (String) principal.getAttributes().get("role");

        // creat jwt
        String token = jwtUtil.createToken(userId, name, role);

//        // ★ 네 필터 규칙에 맞춰 쿠키로 전달
//        Cookie cookie = new Cookie("AUTH", token);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(60 * 60);         // 1h (JwtUtil 만료와 맞추면 더 깔끔)
//        // HTTPS 환경이면:
//        // cookie.setSecure(true);
//        res.addCookie(cookie);

        // 보안 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("AUTH", token)
                .httpOnly(true)
                .secure(isProd())                 // HTTPS 환경에서는 true
                .sameSite(isProd() ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(60))
                .build();

        res.addHeader("Set-Cookie", cookie.toString());

//      수정전   // 프론트 성공 페이지로 이동
//        res.sendRedirect(redirectBase);

        // 수정 후
        // 환경별 리다이렉트
        String target = isLocal() ? "http://localhost:5173/oauth2/success" : redirectBase;
        res.sendRedirect(target);
    }

    private boolean isLocal() {
        return Arrays.asList(env.getActiveProfiles()).contains("local");
    }

    private boolean isProd() {
        return Arrays.asList(env.getActiveProfiles()).contains("prod");
    }

}
