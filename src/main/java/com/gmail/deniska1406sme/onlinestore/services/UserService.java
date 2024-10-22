package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    void updateUser(UserDTO userDTO);

    void deleteUser(Long id);

    boolean userExists(String email);

    UserDTO findUserByEmail(String email);

    UserDTO findUserByGoogleId(String googleId);

    Page<UserDTO> findAllUsers(Pageable pageable);

}
