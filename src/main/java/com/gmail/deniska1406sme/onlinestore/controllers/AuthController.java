package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.dto.LoginRequest;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.services.PasswordAuthenticationService;
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

    @Autowired
    public AuthController(PasswordAuthenticationService passwordAuthenticationService) {
        this.passwordAuthenticationService = passwordAuthenticationService;
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
}
