package com.gmail.deniska1406sme.onlinestore.dto;

import jakarta.validation.Valid;

public class AddClientRequest {

    @Valid
    private ClientDTO clientDTO;

    @Valid
    private UserDTO userDTO;

    private String password;

    public ClientDTO getClientDTO() {
        return clientDTO;
    }

    public void setClientDTO(ClientDTO clientDTO) {
        this.clientDTO = clientDTO;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
