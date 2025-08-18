package com.bivolaris.centralbank.controllers;


import com.bivolaris.centralbank.config.JwtConfig;
import com.bivolaris.centralbank.dtos.BankAuthRequest;
import com.bivolaris.centralbank.dtos.JwtResponse;
import com.bivolaris.centralbank.dtos.LoginRequest;
import com.bivolaris.centralbank.dtos.RegisterRequest;
import com.bivolaris.centralbank.dtos.UserDto;
import com.bivolaris.centralbank.entities.Bank;
import com.bivolaris.centralbank.entities.BankStatus;
import com.bivolaris.centralbank.mappers.UserMapper;
import com.bivolaris.centralbank.repositories.AuthRepository;
import com.bivolaris.centralbank.repositories.BankRepository;
import com.bivolaris.centralbank.services.AuthService;
import com.bivolaris.centralbank.services.CustomUserDetails;
import com.bivolaris.centralbank.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthRepository authRepository;
    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;
    private final BankRepository bankRepository;
    
    @Value("${spring.bank.bankMasterSecret}")
    private String bankMasterSecret;

    




    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {

        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        //Removed the second repository search and getting the id from userDetails instead.
        //This optimization reduced the login queries from 2 to 1.
        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        var accessToken = jwtService.generateAccessToken(userDetails.getAuthId(), userDetails.getRole());
        var refreshToken = jwtService.generateRefreshToken(userDetails.getAuthId(), userDetails.getRole());

        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }


    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken") String refreshToken
    ){

        var jwt = jwtService.parseToken(refreshToken);
        if(jwt == null || jwt.isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var accessToken = jwtService.generateAccessToken(jwt.getUserAuthId(), jwt.getAuthRole());
        return  ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest registerRequest){
        if(!authService.register(registerRequest)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuthId = (Long) authentication.getPrincipal();
        var user = authRepository.findByIdWithEmployee(userAuthId).orElse(null);
        if(user == null){
            return ResponseEntity.notFound().build();
        }

        var employee = user.getEmployee();
        if(employee == null){
            return ResponseEntity.notFound().build();
        }

        var userDto = userMapper.toUserDto(employee);
        return ResponseEntity.ok(userDto);
    }






    /**
     * Endpoint for banks to get inter-bank authentication tokens
     * Requires bank SWIFT code in body and master secret in X-Bank-Secret header
     */
    @PostMapping("/bank-token")
    public ResponseEntity<?> getBankToken(
            @Valid @RequestBody BankAuthRequest request,
            @RequestHeader("Bank-Secret") String bankSecret) {
        
        // Validate master secret from header
        if (!bankMasterSecret.equals(bankSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid bank secret");
        }
        
        // Find bank by SWIFT code
        Bank bank = bankRepository.findAll().stream()
                .filter(b -> request.getSwift().equals(b.getSwift()) && b.getStatus() == BankStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        
        if (bank == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid bank SWIFT code or bank not active");
        }
        

        String bankToken = jwtService.generateBankToken(bank);
        
        return ResponseEntity.ok(new JwtResponse(bankToken));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
