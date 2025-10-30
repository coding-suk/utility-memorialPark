package com.example.memorialparkpart1.web.user.repository;

import com.example.memorialparkpart1.web.user.entity.Provider;
import com.example.memorialparkpart1.web.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
    Optional<User> findByEmail(String email);

}
