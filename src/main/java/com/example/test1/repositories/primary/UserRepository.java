package com.example.test1.repositories.primary;

import com.example.test1.entities.primary.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByemail (String email);

    Optional<User> findByEmail(String email);
}
