package com.bivolaris.centralbank.repositories;



import com.bivolaris.centralbank.entities.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {


    @Query("SELECT e FROM Employee e")
    @EntityGraph(attributePaths = {"auth"})
    List<Employee> findAllEmployees();





    Optional<Employee> findByEmail(@NotEmpty(message = "Email must not be empty.") @Email String email);
}

