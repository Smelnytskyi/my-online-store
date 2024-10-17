package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    void addNewClient(ClientDTO clientDTO, UserDTO userDTO);

    void updateClient(ClientDTO clientDTO, Long id);

    void removeClient(ClientDTO clientDTO);

    List<ClientDTO> getClients(Pageable pageable);

    ClientDTO getClientById(Long id);

    ClientDTO getClientByEmail(String email);

    ClientDTO createTemporaryClient(Long temp);

}
