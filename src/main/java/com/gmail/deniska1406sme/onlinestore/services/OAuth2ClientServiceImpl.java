package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
public class OAuth2ClientServiceImpl implements OAuth2ClientService {

    private final ClientService clientService;


    @Autowired
    public OAuth2ClientServiceImpl(ClientService clientService) {
        this.clientService = clientService;
    }

    public ClientDTO createNewClientFromGoogleData(OAuth2User user, ClientDTO tempClientDTO) {
        String email = (String) user.getAttributes().get("email");
        String googleId = (String) user.getAttributes().get("sub");

        UserDTO userDTO = UserDTO.of(null, email, googleId);
        ClientDTO clientDTO = ClientDTO.of(
                (String) user.getAttributes().get("given_name"),
                (String) user.getAttributes().get("family_name"),
                null,
                null,
                tempClientDTO != null ? tempClientDTO.getCartDTO() : null
        );
        clientService.addNewClient(clientDTO, userDTO);

        return clientDTO;
    }
}
