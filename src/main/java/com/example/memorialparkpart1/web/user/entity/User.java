package com.example.memorialparkpart1.web.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // jpa필수 기본 생성자
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_provider_providerId", columnNames={"provider","providerId"})
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Provider provider;

    @Column(nullable=false, length=64)
    private String providerId; // 카카오/네이버의 고유 id

    @Column(length = 20, unique = true)
    private String email; // 네이버는 거의 제공, 카카오는 동의 필요(없을 수도)

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    void proPersist() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if(role == null) {
            role = Role.USER;
        }
    }

    @Builder
    private User(Provider provider, String providerId, String email, String name, Role role) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.role = role==null? Role.USER : role;
    }

}
