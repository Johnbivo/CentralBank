package com.bivolaris.centralbank.services.impl;


import com.bivolaris.centralbank.dtos.RegisterRequest;
import com.bivolaris.centralbank.entities.Auth;
import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.repositories.AuthRepository;
import com.bivolaris.centralbank.repositories.EmployeeRepository;
import com.bivolaris.centralbank.services.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final AuthRepository authRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;





    @Transactional
    public boolean register(RegisterRequest registerRequest) {
        if (authRepository.findByUsername(registerRequest.getUsername()).isPresent()
                || employeeRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return false;
        }

        try {
            var encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

            var authUser = Auth.createNewAuthForUser(
                    registerRequest.getUsername(),
                    encodedPassword,
                    registerRequest.isAgreedToTerms()
            );

            var employee = Employee.createNewEmployee(
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getEmail(),
                    registerRequest.getPhoneNumber(),
                    registerRequest.getAddress(),
                    registerRequest.getCity(),
                    registerRequest.getProfession()
            );

            authUser.setEmployee(employee);
            authRepository.save(authUser);

            return true;
        } catch (Exception e) {
            return false;
        }
    }



}
