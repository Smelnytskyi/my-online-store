package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.config.JwtTokenProvider;
import com.gmail.deniska1406sme.onlinestore.exceptions.AuthenticationException;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.User;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordAuthenticationServiceImpl implements PasswordAuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public PasswordAuthenticationServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.encoder = new BCryptPasswordEncoder();
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    @Override
    public String authenticate(String login, String rawPassword) {
        User user = userRepository.findByEmail(login);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        String hashedPassword = user.getPassword();
        boolean isPasswordMatch = encoder.matches(rawPassword, hashedPassword);

        if (!isPasswordMatch) {
            throw new AuthenticationException("Wrong password");
        }

        return jwtTokenProvider.createToken(user.getEmail(), user.getRole().name(), user.getId());
    }

    @Transactional
    @Override
    public void savePassword(String login, String rawPassword) {
        String hashedPassword = encoder.encode(rawPassword);
        User user = userRepository.findByEmail(login);

        if (user != null) {
            user.setPassword(hashedPassword);
            userRepository.save(user);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    @Override
    public void changePassword(String login, String oldRawPassword, String newRawPassword) {
        User user = userRepository.findByEmail(login);

        if (user != null) {
            if (encoder.matches(oldRawPassword, user.getPassword())) {
                user.setPassword(encoder.encode(newRawPassword));
                userRepository.save(user);
            } else {
                throw new AuthenticationException("Wrong password");
            }
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    @Override
    public boolean hasPassword(String login){
        User user = userRepository.findByEmail(login);

        if (user == null){
            throw new UserNotFoundException("User not found");
        }

        return user.getPassword() != null && !user.getPassword().isEmpty();
    }
}
