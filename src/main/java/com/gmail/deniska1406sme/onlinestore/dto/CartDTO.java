package com.gmail.deniska1406sme.onlinestore.dto;

import java.util.Set;

public class CartDTO {
    private Set<CartItemDTO> items;
    private Double totalPrice;


    public CartDTO() {
    }

    public CartDTO(Set<CartItemDTO> items, Double totalPrice) {
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public Set<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(Set<CartItemDTO> items) {
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
        return "CartDTO{" +
                "items=" + items +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
