package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.dtos.RegisterRequest;
import com.bivolaris.centralbank.entities.Auth;
import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.repositories.AuthRepository;
import com.bivolaris.centralbank.repositories.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {


    private final AuthRepository authRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;


    public boolean login(String username, String password) {
       var user = authRepository.findByUsername(username).orElse(null);
       if (user == null) {
           return false;
       }
       return passwordEncoder.matches(password, user.getPassword());

    }


    @Transactional
    public boolean register(RegisterRequest registerRequest){

        if (authRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return false;
        }

        if (employeeRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return false;
        }

        var encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        var authUser = Auth.createNewAuthForUser(
                registerRequest.getUsername(),
                encodedPassword,
                registerRequest.isAgreedToTerms());

        authRepository.save(authUser);

        var employee = Employee.createNewEmployee(registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPhoneNumber(),
                registerRequest.getAddress(),
                registerRequest.getCity(),
                registerRequest.getProfession());



        employeeRepository.save(employee);

        return true;

    }
}
