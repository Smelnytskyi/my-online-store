package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.config.JwtTokenProvider;
import com.gmail.deniska1406sme.onlinestore.config.PasswordAuthenticationHandler;
import com.gmail.deniska1406sme.onlinestore.dto.LoginRequest;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.services.OAuth2ClientService;
import com.gmail.deniska1406sme.onlinestore.services.PasswordAuthenticationService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordAuthenticationService passwordAuthenticationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordAuthenticationHandler passwordAuthenticationHandler;
    private final OAuth2ClientService oAuth2ClientService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Autowired
    public AuthController(PasswordAuthenticationService passwordAuthenticationService, JwtTokenProvider jwtTokenProvider,
                          PasswordAuthenticationHandler passwordAuthenticationHandler,
                          OAuth2ClientService oAuth2ClientService) {
        this.passwordAuthenticationService = passwordAuthenticationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordAuthenticationHandler = passwordAuthenticationHandler;
        this.oAuth2ClientService = oAuth2ClientService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpSession session, @RequestBody @Valid LoginRequest loginRequest,
                                   BindingResult bindingResult) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {
            String token = passwordAuthenticationService.authenticate(email, password);
            Long tempClientId = (Long) session.getAttribute("tempClientId");
            passwordAuthenticationHandler.onAuthenticationSuccess(session, token, tempClientId);
            String redirectUrl = (String) session.getAttribute("redirectUrl");

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("token", token);
            responseMap.put("redirectUrl", redirectUrl);

            return ResponseEntity.ok(responseMap);
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.removeAttribute("tempClientId");
        session.removeAttribute("redirectUrl");
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
