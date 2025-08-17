package com.bivolaris.centralbank.repositories;


import com.bivolaris.centralbank.entities.Auth;
import com.bivolaris.centralbank.entities.AuthRole;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<Auth, UUID> {

    Optional<Auth> findByUsername(@NotEmpty(message = "Username must not be empty.") String username);

    @Query("SELECT a FROM Auth a LEFT JOIN FETCH a.employee WHERE a.username = :username")
    Optional<Auth> findByUsernameWithEmployee(@Param("username") String username);


    @Query("SELECT a FROM Auth a LEFT JOIN FETCH a.employee WHERE a.id = :authId")
    Optional<Auth> findByIdWithEmployee(@Param("authId") Long authId);


}

