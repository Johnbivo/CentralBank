package com.bivolaris.centralbank.repositories;


import com.bivolaris.centralbank.entities.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {


    Optional<Employee> findByEmail(@NotEmpty(message = "Email must not be empty.") @Email String email);
}

