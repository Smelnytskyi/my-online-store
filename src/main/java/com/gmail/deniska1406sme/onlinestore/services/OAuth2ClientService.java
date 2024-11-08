package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2ClientService {

     OAuth2AccessToken getAccessToken(String authorizationCode);

     OAuth2User getUserInfo(String accessToken);

     ClientDTO createNewClientFromGoogleData(OAuth2User user, ClientDTO tempClientDTO);
}
