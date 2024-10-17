package com.gmail.deniska1406sme.onlinestore.config;


import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.services.CartService;
import com.gmail.deniska1406sme.onlinestore.services.ClientService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PasswordAuthenticationHandler implements AuthenticationSuccessHandler {

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Long tempClientId = (Long) request.getSession().getAttribute("tempClientId");
        request.getSession().removeAttribute("tempClientId");
        ClientDTO tempClientDTO = (tempClientId != null) ? clientService.getClientById(tempClientId) : null;

        String email = authentication.getName();
        ClientDTO clientDTO = clientService.getClientByEmail(email);

        if (tempClientDTO != null) {
            transferTempCartToClient(clientDTO, tempClientId);
        }

        String token = request.getHeader("Authorization").replace("Bearer ", "");
        UserRole role = jwtTokenProvider.getRole(token);
        String redirectUrl = "/";
        String clientRedirectUrl = (String) request.getSession().getAttribute("redirectAfterLogin");
        request.getSession().removeAttribute("redirectAfterLogin");

        if (role == UserRole.ADMIN) {
            redirectUrl = "/admin";
        } else if (role == UserRole.EMPLOYEE) {
            redirectUrl = "/employee";
        } else if (role == UserRole.CLIENT) {
            if (clientRedirectUrl != null) {
                redirectUrl = clientRedirectUrl;
            } else {
                redirectUrl = "/main";
            }
        }
        response.sendRedirect(redirectUrl);
    }

    private void transferTempCartToClient(ClientDTO clientDTO, Long tempClientId) {
        if (tempClientId != null) {
            ClientDTO tempClientDTO = clientService.getClientById(tempClientId);
            cartService.transferCartToClient(clientDTO, tempClientDTO);
            cartService.removeAllProductsFromCart(tempClientDTO);
            clientService.removeClient(tempClientDTO);
        }
    }

}
