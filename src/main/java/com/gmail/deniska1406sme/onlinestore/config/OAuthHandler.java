package com.gmail.deniska1406sme.onlinestore.config;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.services.CartService;
import com.gmail.deniska1406sme.onlinestore.services.ClientService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuthHandler implements AuthenticationSuccessHandler {

    private final ClientService clientService;
    private final CartService cartService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public OAuthHandler(ClientService clientService, CartService cartService, JwtTokenProvider jwtTokenProvider) {
        this.clientService = clientService;
        this.cartService = cartService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = token.getPrincipal();

        Map<String, Object> attributes = user.getAttributes();
        String email = (String) attributes.get("email");
        String googleId = (String) attributes.get("sub");

        ClientDTO existingClient = clientService.getClientByEmail(email);
        String tokenJwt = "";

        Long tempClientId = (Long) request.getSession().getAttribute("tempClientId");
        request.getSession().removeAttribute("tempClientId");
        ClientDTO tempClientDTO = (tempClientId != null) ? clientService.getClientById(tempClientId) : null;

        if (existingClient == null) {
            UserDTO userDTO = UserDTO.of(null, email, googleId);
            ClientDTO clientDTO = ClientDTO.of(
                    (String) attributes.get("given_name"),
                    (String) attributes.get("family_name"),
                    null,
                    null,
                    tempClientDTO != null ? tempClientDTO.getCartDTO() : null
            );

            clientService.addNewClient(clientDTO, userDTO);
            tokenJwt = jwtTokenProvider.createToken(email, UserRole.CLIENT.name(), clientService.getClientByEmail(email).getId());
        } else {
            transferTempCartToClient(existingClient, tempClientId);
            tokenJwt = jwtTokenProvider.createToken(email, UserRole.CLIENT.name(), existingClient.getId());
        }

        String redirectUrl = (String) request.getSession().getAttribute("redirectAfterLogin");
        request.getSession().removeAttribute("redirectAfterLogin");

        response.addHeader("Authorization", "Bearer " + tokenJwt);

        if (redirectUrl != null) {
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/main");
        }
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
