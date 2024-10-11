package com.gmail.deniska1406sme.onlinestore.specification;

import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (name == null) ? null : (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Product> hasCategory(ProductCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Product> hasMinPrice(double price) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), price);
    }

    public static Specification<Product> hasMaxPrice(double price) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), price);
    }

}
