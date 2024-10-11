package com.gmail.deniska1406sme.onlinestore.dto;


import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClientDTO {
    private Long id;

    @NotBlank(message = "First name must not be blank", groups = OnCreate.class)
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters", groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @NotBlank(message = "First name must not be blank", groups = OnCreate.class)
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters", groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @NotBlank(message = "Phone must not be blank", groups = OnCreate.class)
    @Size(min = 10, max = 13, message = "Phone must be between 10 and 13 characters", groups = {OnCreate.class, OnUpdate.class})
    private String phone;

    private String address;
    private CartDTO cartDTO;

    public ClientDTO() {
    }

    public ClientDTO(String firstName, String lastName, String phone, String address, CartDTO cartDTO) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.cartDTO = cartDTO;
    }

    public ClientDTO(Long id, String firstName, String lastName, String phone, String address, CartDTO cartDTO) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.cartDTO = cartDTO;
    }

    public static ClientDTO of(String firstName, String lastName, String address, String phone, CartDTO cartDTO) {
        return new ClientDTO(null, firstName, lastName, address, phone, cartDTO);
    }

    public static ClientDTO of(Long id, String firstName, String lastName, String address, String phone, CartDTO cartDTO) {
        return new ClientDTO(id, firstName, lastName, address, phone, cartDTO);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CartDTO getCartDTO() {
        return cartDTO;
    }

    public void setCartDTO(CartDTO cartDTO) {
        this.cartDTO = cartDTO;
    }

    @Override
    public String toString() {
        return "ClientDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", cartDTO=" + cartDTO +
                '}';
    }
}
