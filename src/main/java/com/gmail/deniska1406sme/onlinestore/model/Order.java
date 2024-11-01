package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.OrderDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Client_id")
    private Client client;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String deliveryAddress;

    private String notes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Set<OrderItem> orderItems = new HashSet<>();

    public Order() {
    }

    public Order(Client client, LocalDateTime orderDate, OrderStatus orderStatus, String deliveryAddress, String notes,
                 Set<CartItemDTO> cartItems) {
        this.client = client;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
        for (CartItemDTO item : cartItems) {
            this.orderItems.add(new OrderItem(this, item.getProductId(), item.getQuantity()));
        }
    }

    public OrderDTO toOrderDTO() {
        Set<CartItemDTO> cartItemDTOS = new HashSet<>();
        for (OrderItem item : orderItems) {
            cartItemDTOS.add(new CartItemDTO(item.getProductId(), item.getQuantity()));
        }
        return new OrderDTO(id, orderDate, orderStatus, deliveryAddress, notes, cartItemDTOS);
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", client=" + client +
                ", orderDate=" + orderDate +
                ", orderStatus=" + orderStatus +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", notes='" + notes + '\'' +
                ", orderItemsCount=" + orderItems.size() +
                '}';
    }
}
