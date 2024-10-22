package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.User;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    @Override
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    @Override
    public UserDTO findUserByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            User user = userRepository.findByEmail(email);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            return userDTO;
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    @Override
    public UserDTO findUserByGoogleId(String googleId) {
        User user = userRepository.findByGoogleId(googleId);
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setGoogleId(user.getGoogleId());
            return userDTO;
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    @Override
    public Page<UserDTO> findAllUsers(Pageable pageable) {
        Page<User> userDTOS = userRepository.findAll(pageable);
        return userDTOS.map(User::toUserDTO);
    }

    @Transactional
    @Override
    public void updateUser(UserDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getGoogleId() != null) {
            existingUser.setGoogleId(userDTO.getGoogleId());
        }
        userRepository.save(existingUser);
    }
}
