package com.gmail.deniska1406sme.onlinestore.config;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.model.UserRole;
import com.gmail.deniska1406sme.onlinestore.services.CartService;
import com.gmail.deniska1406sme.onlinestore.services.ClientService;
import com.gmail.deniska1406sme.onlinestore.services.OAuth2ClientService;
import com.gmail.deniska1406sme.onlinestore.services.OAuth2ClientServiceImpl;
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
    private final OAuth2ClientService oAuth2ClientService;

    @Autowired
    public OAuthHandler(ClientService clientService, CartService cartService, JwtTokenProvider jwtTokenProvider,
                        OAuth2ClientServiceImpl oauth2ClientService) {
        this.clientService = clientService;
        this.cartService = cartService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.oAuth2ClientService = oauth2ClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        System.out.println("йоу я начал выполнятся");
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = token.getPrincipal();

        Map<String, Object> attributes = user.getAttributes();
        String email = (String) attributes.get("email");

        ClientDTO existingClient = clientService.getClientByEmail(email);
        String tokenJwt;

        Long tempClientId = (Long) request.getSession().getAttribute("tempClientId");
        System.out.println(tempClientId);
        request.getSession().removeAttribute("tempClientId");
        ClientDTO tempClientDTO = (tempClientId != null) ? clientService.getClientById(tempClientId) : null;

        if (existingClient == null){
            ClientDTO newClientDTO = oAuth2ClientService.createNewClientFromGoogleData(user, tempClientDTO);
            tokenJwt = jwtTokenProvider.createToken(email, UserRole.CLIENT.name(), clientService.getClientByEmail(email).getId());
            System.out.println(tokenJwt);
        } else {
            transferTempCartToClient(existingClient, tempClientId);
            tokenJwt = jwtTokenProvider.createToken(email, UserRole.CLIENT.name(), existingClient.getId());
        }

        response.addHeader("Authorization", "Bearer " + tokenJwt);
        response.sendRedirect("/index.html?token=" + tokenJwt);
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
