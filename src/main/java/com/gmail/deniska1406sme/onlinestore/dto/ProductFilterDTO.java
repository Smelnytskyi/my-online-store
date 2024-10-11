package com.gmail.deniska1406sme.onlinestore.dto;

import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import jakarta.validation.constraints.Min;


public class ProductFilterDTO {
    private String productName;
    private ProductCategory category;

    @Min(value = 0, message = "Min price should not be less than 0")
    private Double minPrice;

    private Double maxPrice;

    public ProductFilterDTO() {
    }

    public ProductFilterDTO(String productName, ProductCategory category, Double minPrice, Double maxPrice) {
        this.productName = productName;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    @Override
    public String toString() {
        return "ProductFilterDTO{" +
                "productName='" + productName + '\'' +
                ", category=" + category +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                '}';
    }
}
