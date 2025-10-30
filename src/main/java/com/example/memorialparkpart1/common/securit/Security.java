package com.example.memorialparkpart1.common.securit;

import com.example.memorialparkpart1.common.handler.OAuth2SuccessHandler;
import com.example.memorialparkpart1.common.jwt.JwtAuthFilter;
import com.example.memorialparkpart1.web.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class Security {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(req -> {
                    var c = new org.springframework.web.cors.CorsConfiguration();
                    c.setAllowCredentials(true);
                    c.setAllowedOrigins(List.of(
                            "http://localhost:5173",           // 개발 프론트
                            "https://memorialpark.site"     // 운영 프론트
                    ));
                    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    c.setAllowedHeaders(List.of("*"));
                    c.setExposedHeaders(List.of("Set-Cookie"));
                    return c;
                }))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/health", "/public/**",
                                        "/oauth2/**", "/oauth2/authorization/**",
                                        "/login/**",
                                        "/login/oauth2/code/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
//                .authorizeHttpRequests(auth -> auth
//                // 1) 정적 리소스: 안전하게 한 줄로
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
//                // 2) 공개 엔드포인트
//                .requestMatchers("/auth/**", "/login.html", "/mypage.html", "/adminpage.html").permitAll()
//                // 3) 관리자
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//                // 4) 나머지는 인증 필요
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );
//        http. addFilterBefore(new JwtAuthFilter(jwtUtil),
//                UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
