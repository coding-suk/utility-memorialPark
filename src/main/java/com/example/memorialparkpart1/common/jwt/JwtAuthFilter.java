package com.example.memorialparkpart1.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtAuthFilter(JwtUtil jwt) {
        this.jwt = jwt;
    }

    /** 정적/공개 경로는 필터를 타지 않게 건너뛴다 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getServletPath();
        return
                p.startsWith("/auth/") ||
                        p.equals("/") ||
                        p.equals("/login.html") ||
                        p.startsWith("/login/oauth2/code/") ||
                        p.startsWith("/oauth2") ||
                        p.equals("/mypage.html") ||
                        p.equals("/adminpage.html") ||
                        matcher.match("/**/*.css", p) ||
                        matcher.match("/**/*.js",  p) ||
                        matcher.match("/**/*.png", p) ||
                        matcher.match("/**/*.ico", p) ||
                        matcher.match("/**/*.txt", p);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        try {
            String token = null;
            Cookie[] cs = req.getCookies();
            if (cs != null) {
                for (Cookie c : cs) {
                    if ("AUTH".equals(c.getName())) {
                        token = c.getValue();
                        break; // 토큰 찾으면 루프 종료
                    }
                }
            }

            if (token != null && !token.isBlank()) {
                var jws  = jwt.parse(token);
                var body = jws.getBody();

                String userId = body.getSubject();
                String role   = body.get("role", String.class);

                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                SecurityContextHolder.clearContext(); // 토큰 없으면 미인증 상태로 통과
            }
        } catch (Exception e) {
            // 토큰 오류 → 미인증으로 통과 (여기서 응답 만들지 않음)
            SecurityContextHolder.clearContext();
        }

        // ★★★ 항상 다음 필터/서블릿으로 넘겨야 화면이 뜬다
        chain.doFilter(req, res);
    }
}
