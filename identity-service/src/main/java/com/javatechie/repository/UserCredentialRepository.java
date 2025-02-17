package com.javatechie.repository;

import com.javatechie.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository  extends JpaRepository<User,Long> {
    Optional<User> findByName(String username);

    User findByEmail(String email);
}
