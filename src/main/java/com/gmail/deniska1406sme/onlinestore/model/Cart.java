package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.CartDTO;
import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import jakarta.persistence.*;

import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "Carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "Client_id")
    private Client client;

    @OneToMany
    private Set<CartItem> items;

    private Double totalPrice;

    public Cart() {
    }

    public Cart(Client client, Set<CartItem> cartItem, Double totalPrice) {
        this.client = client;
        this.items = cartItem;
        this.totalPrice = totalPrice;
    }

    public CartDTO toCartDTO() {
        Set<CartItemDTO> cartItemDTOs = items.stream()
                .map(CartItem::toCartItemDTO)
                .collect(Collectors.toSet());
        return new CartDTO(cartItemDTOs, totalPrice);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Set<CartItem> getItems() {
        return items;
    }

    public void setItems(Set<CartItem> items) {
        this.items = items;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", client=" + client +
                ", items=" + items +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
