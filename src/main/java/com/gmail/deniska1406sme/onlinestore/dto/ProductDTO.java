package com.gmail.deniska1406sme.onlinestore.dto;

import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name must not be blank", groups = OnCreate.class)
    @Size(max = 200, message = "Name must not be grater than 200 characters", groups = {OnCreate.class, OnUpdate.class})
    private String name;

    private ProductCategory category;

    @NotBlank(message = "Description must not be blank", groups = OnCreate.class)
    @Size(max = 2000, message = "Description must not be grater than 200 characters", groups = {OnCreate.class, OnUpdate.class})
    private String description;

    @NotNull(message = "Price must not be blank", groups = OnCreate.class)
    @Min(value = 0, message = "Price must not be negative", groups = {OnCreate.class, OnUpdate.class})
    private Double price;

    private String imageUrl;
    private String deleteImageUrl;

    @NotNull(message = "Quantity must not be blank", groups = OnCreate.class)
    @Min(value = 0, message = "Quantity must not be negative", groups = {OnCreate.class, OnUpdate.class})
    private int quantity;
    private Map<String, String> attributes = new HashMap<>();

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, ProductCategory category, String description, Double price, int quantity,
                      String imageUrl, String deleteImageUrl, Map<String, String> attributes) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.deleteImageUrl = deleteImageUrl;
        this.attributes = attributes;
    }

    public static ProductDTO of(Long id, String name, ProductCategory category, String description, Double price, int quantity,
                                String imageUrl, String deleteImageUrl, Map<String, String> attributes) {
        return new ProductDTO(id, name, category, description, price, quantity, imageUrl, deleteImageUrl, attributes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDeleteImageUrl() {
        return deleteImageUrl;
    }

    public void setDeleteImageUrl(String deleteImageUrl) {
        this.deleteImageUrl = deleteImageUrl;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", deleteImageUrl='" + deleteImageUrl + '\'' +
                ", quantity=" + quantity +
                ", attributes=" + attributes +
                '}';
    }
}
