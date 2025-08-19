package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.dtos.RegisterRequest;

public interface AuthService {

    public boolean register(RegisterRequest registerRequest);
}
