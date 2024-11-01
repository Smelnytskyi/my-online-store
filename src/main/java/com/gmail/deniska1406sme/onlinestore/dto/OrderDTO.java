package com.gmail.deniska1406sme.onlinestore.dto;

import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Set;

public class OrderDTO {
    private Long id;

    @NotBlank(message = "Order date must not be blank")
    private LocalDateTime orderDate;

    @NotBlank(message = "Order status must not be blank")
    private OrderStatus orderStatus;

    @NotBlank(message = "Delivery address must not be blank")
    private String deliveryAddress;
    private String notes;

    private Set<CartItemDTO> items;

    private String clientFirstName;
    private String clientLastName;

    public OrderDTO() {
    }

    public OrderDTO(Long id, LocalDateTime orderDate, OrderStatus orderStatus, String deliveryAddress, String notes,
                    Set<CartItemDTO> items) {
        this.id = id;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(Set<CartItemDTO> items) {
        this.items = items;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public void setClientFirstName(String clientFirstName) {
        this.clientFirstName = clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", orderStatus=" + orderStatus +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", notes='" + notes + '\'' +
                ", items=" + items +
                '}';
    }
}
