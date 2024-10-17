package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import jakarta.persistence.*;

@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "Product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "Cart_id")
    private Cart cart;

    public CartItem() {
    }

    public CartItem(int quantity, Product product) {
        this.quantity = quantity;
        this.product = product;
    }

    public CartItemDTO toCartItemDTO() {
        return new CartItemDTO(product.getId(), quantity);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", product=" + product +
                ", cart=" + cart +
                '}';
    }
}
