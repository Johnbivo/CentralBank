package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.dtos.RegisterRequest;
import com.bivolaris.centralbank.dtos.ResetPasswordRequest;
import com.bivolaris.centralbank.entities.Auth;
import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.repositories.AuthRepository;
import com.bivolaris.centralbank.repositories.EmployeeRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

            // establish relationship
            authUser.setEmployee(employee);

            // only need one save because of CascadeType.ALL
            authRepository.save(authUser);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {
       var oldPassword =  resetPasswordRequest.getOldPassword();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
       var confirmPassword = resetPasswordRequest.getConfirmPassword();
       if (!password.equals(confirmPassword)) {
           return false;
       }

    }

    public

}
