package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Client;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import com.gmail.deniska1406sme.onlinestore.services.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testDeleteUserSuccess() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    public void testDeleteUserNotFound() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    public void testUserExist() {
        String email = "test@gmail.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean exists = userService.userExists(email);

        assertTrue(exists);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    public void testUserNotExist() {
        String email = "test@gmail.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean exists = userService.userExists(email);

        assertFalse(exists);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    public void testFindUserByEmailSuccess() {
        String email = "test@gmail.com";
        Client client = new Client(email, UserRole.CLIENT, "denys", "smel", "213124154");

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(client);

        UserDTO userDTO = userService.findUserByEmail(email);

        assertNotNull(userDTO);
        assertEquals(email, userDTO.getEmail());
        verify(userRepository).existsByEmail(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testFindUserByEmailNotFound() {
        String email = "test@gmail.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(email));

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).findByEmail(email);
    }

    @Test
    public void testFindUserByGoogleIdSuccess() {
        String googleId = "test-google-id";
        Client client = new Client("", "", googleId, "", "", "", "");
        when(userRepository.findByGoogleId(googleId)).thenReturn(client);

        UserDTO userDTO = userService.findUserByGoogleId(googleId);

        assertNotNull(userDTO);
        assertEquals(googleId, userDTO.getGoogleId());
        verify(userRepository).findByGoogleId(googleId);
    }

    @Test
    public void testFindUserByGoogleIdNotFound() {
        String googleId = "test-google-id";
        when(userRepository.findByGoogleId(googleId)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.findUserByGoogleId(googleId));

        verify(userRepository).findByGoogleId(googleId);
    }

}
