package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Cart;
import com.gmail.deniska1406sme.onlinestore.model.Client;
import com.gmail.deniska1406sme.onlinestore.model.Order;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.repositories.ClientRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public void updateClient(ClientDTO clientDTO, Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Client not found"));

        if (clientDTO.getFirstName() != null) {
            client.setFirstName(clientDTO.getFirstName());
        }
        if (clientDTO.getLastName() != null) {
            client.setLastName(clientDTO.getLastName());
        }
        if (clientDTO.getPhone() != null) {
            client.setPhone(clientDTO.getPhone());
        }
        if (clientDTO.getAddress() != null) {
            client.setAddress(clientDTO.getAddress());
        }
        clientRepository.save(client);
    }

    @Transactional
    @Override
    public void addNewClient(ClientDTO clientDTO, UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Cart newCart = new Cart();
        newCart.setItems(new HashSet<>());
        newCart.setTotalPrice(0.0); //create empty Cart which has relation with new Client

        Set<Order> orders = new HashSet<>(); //create empty Order list which has relation with new Client

        Client client = Client.fromDTO(userDTO, clientDTO);
        client.setCart(newCart);
        client.setOrders(orders);
        newCart.setClient(client);
        for (Order order : orders) {
            order.setClient(client);//create relationship
        }

        clientRepository.save(client);
    }

    @Transactional
    @Override
    public void removeClient(ClientDTO clientDTO) {
        Client client = clientRepository.findById(clientDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("Client not found"));
        clientRepository.delete(client);
    }

    @Transactional
    @Override
    public ClientDTO getClientById(Long id) {
        if (id == null) {
            return null;
        }

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Client not found"));
        return client.toClientDTO();
    }


    @Transactional
    @Override
    public List<ClientDTO> getClients(Pageable pageable) {

        Page<Client> clients = clientRepository.findAll(pageable);

        List<ClientDTO> clientDTOs = clients.getContent().stream()
                .map(Client::toClientDTO)
                .collect(Collectors.toList());
        return clientDTOs;
    }

    @Transactional
    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email);
        if (client == null) {
            return null;
        }
        return client.toClientDTO();
    }

    @Transactional
    @Override
    public ClientDTO createTemporaryClient(Long temp) {
        Cart newCart = new Cart();
        newCart.setItems(new HashSet<>());
        newCart.setTotalPrice(0.0);

        Client client = new Client("temp" + temp, UserRole.TEMPORARY, "temp" + temp, "temp" + temp, "temp" + temp);
        client.setCart(newCart);
        newCart.setClient(client);

        clientRepository.save(client);
        return client.toClientDTO();
    }

}
