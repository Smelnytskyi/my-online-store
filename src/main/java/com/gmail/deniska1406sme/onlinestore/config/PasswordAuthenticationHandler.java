package com.gmail.deniska1406sme.onlinestore.config;


import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.services.CartService;
import com.gmail.deniska1406sme.onlinestore.services.ClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PasswordAuthenticationHandler {

    private final ClientService clientService;
    private final CartService cartService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public PasswordAuthenticationHandler(ClientService clientService, CartService cartService,
                                         JwtTokenProvider jwtTokenProvider) {
        this.clientService = clientService;
        this.cartService = cartService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void onAuthenticationSuccess(HttpSession session, String token, Long tempClientId) {
        UserRole role = jwtTokenProvider.getRole(token);
        String redirectUrl = "/";

        ClientDTO tempClientDTO = (tempClientId != null) ? clientService.getClientById(tempClientId) : null;

        if (role == UserRole.CLIENT) {
            ClientDTO clientDTO = clientService.getClientByEmail(jwtTokenProvider.getLogin(token));

            if (tempClientDTO != null) {
                cartService.transferCartToClient(clientDTO, tempClientDTO);
                deleteTempClient(tempClientDTO);
            }
            redirectUrl = "/profile-client.html";
        } else if (role == UserRole.ADMIN) {
            if (tempClientDTO != null) {
                deleteTempClient(tempClientDTO);
            }
            redirectUrl = "/profile-admin.html";
        } else if (role == UserRole.EMPLOYEE) {
            if (tempClientDTO != null) {
                deleteTempClient(tempClientDTO);
            }
            redirectUrl = "/profile-employee.html";
        }

        // Перенаправление на соответствующую страницу профиля
        try {
            session.setAttribute("redirectUrl", redirectUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTempClient(ClientDTO tempClientDTO) {
        cartService.removeAllProductsFromCart(tempClientDTO);
        clientService.removeClient(tempClientDTO);
    }
}
