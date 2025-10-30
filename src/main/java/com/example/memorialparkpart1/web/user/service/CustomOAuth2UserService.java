package com.example.memorialparkpart1.web.user.service;

import com.example.memorialparkpart1.web.user.entity.Provider;
import com.example.memorialparkpart1.web.user.entity.Role;
import com.example.memorialparkpart1.web.user.entity.User;
import com.example.memorialparkpart1.web.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(req);

        String registrationId = req.getClientRegistration().getRegistrationId(); // 카카오, 네이버
        Map<String, Object> attrs = oAuth2User.getAttributes();

        OAuthProfile profile = switch (registrationId) {
            case "kakao" -> KakaoProfile.from(attrs);
            case "naver" -> NaverProfile.from(attrs);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };

        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        User user = userRepository.findByProviderAndProviderId(provider, profile.id())
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider(provider)
                        .providerId(profile.id())
                        .email(profile.email().orElse(null))
                        .name(profile.name())
                        .role(Role.USER)  // 기본 역활 지정
                        .build()));

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new DefaultOAuth2User(authorities, Map.of(
                "userId", String.valueOf(user.getId()),
                "name", user.getName(),
                "role", user.getRole().name()
        ), "name");
    }

    // compact record로 표준화
    record OAuthProfile(String id, String name, Optional<String> email) {
    }

    static class KakaoProfile {
        static OAuthProfile from(Map<String, Object> a) {
            String id = String.valueOf(a.get("id"));
            Map<String, Object> acc = (Map<String, Object>) a.getOrDefault("kakao_account", Map.of());
            Map<String, Object> prof = (Map<String, Object>) acc.getOrDefault("profile", Map.of());
            String name = (String) prof.getOrDefault("nickname", "카카오유저");
            String email = (String) acc.get("email");
            return new OAuthProfile(id, name, Optional.ofNullable(email));
        }
    }

    static class NaverProfile {
        static OAuthProfile from(Map<String, Object> a) {
            Map<String, Object> resp = (Map<String, Object>) a.getOrDefault("response", Map.of());
            String id = String.valueOf(resp.get("id"));
            String name = (String) resp.getOrDefault("name", "네이버유저");
            String email = (String) resp.get("email");
            return new OAuthProfile(id, name, Optional.ofNullable(email));
        }
    }

}

