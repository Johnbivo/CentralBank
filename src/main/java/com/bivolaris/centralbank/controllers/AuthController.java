package com.bivolaris.centralbank.controllers;


import com.bivolaris.centralbank.dtos.LoginRequest;
import com.bivolaris.centralbank.dtos.RegisterRequest;
import com.bivolaris.centralbank.repositories.AuthRepository;
import com.bivolaris.centralbank.services.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/centralbank/auth")
public class AuthController {


    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request){
        if(!authService.login(request.getUsername(), request.getPassword())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest registerRequest){
        if(!authService.register(registerRequest)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
