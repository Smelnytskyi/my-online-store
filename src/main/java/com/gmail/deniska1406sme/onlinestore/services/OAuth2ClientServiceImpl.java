package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class OAuth2ClientServiceImpl implements OAuth2ClientService {

    private final ClientService clientService;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String redirectUri;

    @Autowired
    public OAuth2ClientServiceImpl(ClientService clientService,
                                   @Value("${google.client-id}") String googleClientId,
                                   @Value("${google.client-secret}") String googleClientSecret,
                                   @Value("${google.redirect-uri}") String redirectUri) {

        this.clientService = clientService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.redirectUri = redirectUri;
    }

    public OAuth2AccessToken getAccessToken(String authorizationCode) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        // Получаем токен из ответа
        String accessTokenValue = (String) response.getBody().get("access_token");
        String tokenType = (String) response.getBody().get("token_type");

        // Создаем объект OAuth2AccessToken
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessTokenValue, null, null);
        return accessToken;
    }

    public OAuth2User getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);

        Map<String, Object> attributes = response.getBody();
        return new DefaultOAuth2User(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes, "email");
    }

    public ClientDTO createNewClientFromGoogleData(OAuth2User user, ClientDTO tempClientDTO) {
        String email = (String) user.getAttributes().get("email");
        String googleId = (String) user.getAttributes().get("sub");

        // Создание нового клиента в базе
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
