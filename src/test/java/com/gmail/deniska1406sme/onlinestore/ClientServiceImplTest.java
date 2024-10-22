package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Client;
import com.gmail.deniska1406sme.onlinestore.repositories.ClientRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import com.gmail.deniska1406sme.onlinestore.services.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    public void testUpdateClientExistingClient() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setFirstName("Denis");

        Client existingClient = new Client();
        existingClient.setId(id);
        existingClient.setFirstName("Denys");

        when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));

        clientService.updateClient(clientDTO, id);

        assertEquals("Denis", existingClient.getFirstName());
        verify(clientRepository).findById(id);
        verify(clientRepository).save(existingClient);
    }

    @Test
    public void testUpdateClientNonExistingClient() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setFirstName("Denis");

        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clientService.updateClient(clientDTO, id));
    }

    @Test
    public void testAddNewClientExistingClient() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setFirstName("Denis");

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("email@email.com");

        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> clientService.addNewClient(clientDTO, userDTO));
    }

    @Test
    public void testRemoveClientExistingClient() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(id);

        Client existingClient = new Client();
        existingClient.setId(id);

        when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));

        clientService.removeClient(clientDTO);
        verify(clientRepository).delete(existingClient);
    }

    @Test
    public void testRemoveClientNonExistingClient() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(id);

        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clientService.removeClient(clientDTO));
    }

    @Test
    public void testGetClientByIdExistingClient() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(id);

        Client existingClient = new Client();
        existingClient.setId(id);

        when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));

        ClientDTO res = clientService.getClientById(id);

        assertNotNull(res);
        assertEquals(id, res.getId());
    }

    @Test
    public void testGetClientByIdNonExistingClient() {
        Long id = 1L;

        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clientService.getClientById(id));
    }
}
