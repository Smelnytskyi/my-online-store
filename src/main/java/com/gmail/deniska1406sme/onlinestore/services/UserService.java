package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    void updateUser(UserDTO userDTO);

    void deleteUser(Long id);

    boolean userExists(String email);

    UserDTO findUserByEmail(String email);

    UserDTO findUserByGoogleId(String googleId);

    List<UserDTO> findAllUsers(Pageable pageable);

}
