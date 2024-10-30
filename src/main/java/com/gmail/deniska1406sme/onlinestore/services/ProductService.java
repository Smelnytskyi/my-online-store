package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ProductFilterDTO;
import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductService {

    void addProduct(ProductDTO product);

    void updateProduct(ProductDTO product, Long id);

    void removeProduct(Long id);

    void updateProductQuantity(Long id, Integer quantity);

    Page<ProductDTO> findProductByName(String name, Pageable pageable);

    Page<ProductDTO> findByQuantityLessThan(Pageable pageable, int quantity);

    Page<ProductDTO> getAllProducts(Pageable pageable);

    Page<ProductDTO> getProductsByCategory(ProductCategory category, Pageable pageable);

    Page<ProductDTO> searchProductByAttributes(String category, Map<String, List<String>> filters, Pageable pageable);

    ProductDTO getProductById(Long id);
}
