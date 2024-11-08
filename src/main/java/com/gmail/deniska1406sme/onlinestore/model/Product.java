package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "Products", indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_price", columnList = "price"),
        @Index(name = "idx_category", columnList = "category")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private int quantity;

    @Column
    private String imageUrl;

    @Column
    private String deleteImageUrl;

    @ElementCollection
    @CollectionTable(name = "Product_attributes",
            joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    public Product() {
    }

    public Product(String name, ProductCategory category, String description, Double price, int quantity, String imageUrl,
                   String deleteImageUrl, Map<String, String> attributes) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.deleteImageUrl = deleteImageUrl;
        this.attributes = attributes;
    }

    public static Product of(String name, ProductCategory category, String description, Double price, int quantity,
                             String imageUrl, String deleteImageUrl, Map<String, String> attributes) {
        return new Product(name, category, description, price, quantity, imageUrl, deleteImageUrl, attributes);
    }

    public ProductDTO toProductDTO() {
        return new ProductDTO(id, name, category, description, price, quantity, imageUrl, deleteImageUrl, attributes);
    }

    public static Product fromProductDTO(ProductDTO productDTO) {
        return Product.of(productDTO.getName(), productDTO.getCategory(), productDTO.getDescription(), productDTO.getPrice(),
                productDTO.getQuantity(), productDTO.getImageUrl(), productDTO.getDeleteImageUrl(), productDTO.getAttributes());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", imageUrl='" + imageUrl + '\'' +
                ", deleteImageUrl='" + deleteImageUrl + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
