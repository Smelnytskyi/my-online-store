package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.CartDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Client extends User {

    @Column(nullable = false, length = 30)
    private String firstName;

    @Column(nullable = false, length = 30)
    private String lastName;

    @Column(unique = true, nullable = false, length = 15)
    private String phone;
    private String address;

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Order> orders;


    public Client() {
        super();
    }

    public Client(String email, UserRole role, String firstName, String lastName, String phone) {
        super(email, role);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public Client(String email, String password, String googleId, String firstName, String lastName, String address,
                  String phone) {
        super(email, password, googleId, UserRole.CLIENT);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
    }

    public static Client of(String email, String password, String googleId, String firstName, String lastName,
                            String address, String phone) {
        return new Client(email, password, googleId, firstName, lastName, address, phone);
    }

    public ClientDTO toClientDTO() {
        UserDTO userDTO = super.toUserDTO();
        CartDTO cartDTO = (cart != null) ? cart.toCartDTO() : null;
        return ClientDTO.of(userDTO.getId(), firstName, lastName, phone, address, cartDTO);
    }

    public static Client fromDTO(UserDTO userDTO, ClientDTO clientDTO) {
        return Client.of(userDTO.getEmail(), null, userDTO.getGoogleId(), clientDTO.getFirstName(),
                clientDTO.getLastName(), clientDTO.getAddress(), clientDTO.getPhone());
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

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + getId() +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
