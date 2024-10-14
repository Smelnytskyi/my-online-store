package com.gmail.deniska1406sme.onlinestore.dto;

import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartItemDTO {

    @NotNull(message = "Id must not be null", groups = OnUpdate.class)
    private Long productId;

    @NotNull(message = "Quantity must not be null", groups = OnUpdate.class)
    @Min(value = 1, message = "Quantity must not be negative", groups = OnUpdate.class)
    private int quantity;

    public CartItemDTO() {
    }

    public CartItemDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItemDTO{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
