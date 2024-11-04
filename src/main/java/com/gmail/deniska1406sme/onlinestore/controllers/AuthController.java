package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.config.JwtTokenProvider;
import com.gmail.deniska1406sme.onlinestore.dto.LoginRequest;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.services.PasswordAuthenticationService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordAuthenticationService passwordAuthenticationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(PasswordAuthenticationService passwordAuthenticationService, JwtTokenProvider jwtTokenProvider) {
        this.passwordAuthenticationService = passwordAuthenticationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult bindingResult) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {
            String token = passwordAuthenticationService.authenticate(email, password);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неправильный логин");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неправильный пароль");
        }
    }

    @GetMapping("/role")
    public ResponseEntity<?> getRole(@RequestHeader(value = "Authorization") String token) {
        try {
            UserRole role = jwtTokenProvider.getRole(token);
            return ResponseEntity.ok(Collections.singletonMap("role", role));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный или просроченный токен");
        }
    }
}
