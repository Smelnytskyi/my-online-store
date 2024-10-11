package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.model.User;

public interface PasswordAuthenticationService {

    String authenticate(String login, String password);
    void savePassword(String login, String rawPassword);
    void changePassword(String login, String oldRawPassword, String newRawPassword);


}
