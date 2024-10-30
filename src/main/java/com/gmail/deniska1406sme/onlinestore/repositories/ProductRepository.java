package com.gmail.deniska1406sme.onlinestore.repositories;

import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByQuantityLessThan(Pageable pageable, int quantity);

    Page<Product> findProductsByNameLikeIgnoreCase(String name, Pageable pageable);

    boolean existsByName(String name);

    Page<Product> findProductsByCategory(ProductCategory category, Pageable pageable);

}
