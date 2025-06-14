package com.javatechie.repository;

import com.javatechie.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCredentialRepository  extends JpaRepository<User,Long> {
    Optional<User> findByName(String username);

    User findByEmail(String email);

    @Query(
            value = "SELECT id FROM user ORDER BY rand() LIMIT 1000",
            nativeQuery = true
    )
    List<Long> findRandomUsers();
}
