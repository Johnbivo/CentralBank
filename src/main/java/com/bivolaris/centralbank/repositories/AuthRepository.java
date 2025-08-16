package com.bivolaris.centralbank.repositories;


import com.bivolaris.centralbank.entities.Auth;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<Auth, UUID> {

    Optional<Auth> findByUsername(@NotEmpty(message = "Username must not be empty.") String username);

}
