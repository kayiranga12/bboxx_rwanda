package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    List<User> findByRole(String technician);
}
