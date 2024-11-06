package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.ProductNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import com.gmail.deniska1406sme.onlinestore.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;

    public ProductServiceImpl(ProductRepository productRepository, ImageService imageService) {
        this.productRepository = productRepository;
        this.imageService = imageService;
    }

    @Transactional
    @Override
    public void addProduct(ProductDTO productDTO) {
        if (productRepository.existsByName(productDTO.getName())) {
            throw new IllegalArgumentException("Product already exists");
        }
        imageService.uploadImage(productDTO.getImageUrl(), productDTO);

        Product product = new Product(
                productDTO.getName(),
                productDTO.getCategory(),
                productDTO.getDescription(),
                productDTO.getPrice(),
                productDTO.getQuantity(),
                productDTO.getImageUrl(),
                productDTO.getDeleteImageUrl(),
                productDTO.getAttributes()
        );
        productRepository.save(product);
    }

    @Transactional
    @Override
    public void removeProduct(Long id) {
        if (productRepository.existsById(id)) {
            imageService.deleteImage(productRepository.findById(id).get().getDeleteImageUrl());
            productRepository.deleteById(id);
        } else {
            throw new ProductNotFoundException("Product does not exist");
        }
    }

    @Transactional
    @Override
    public void updateProduct(ProductDTO productDTO, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product does not exist"));
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getCategory() != null) {
            product.setCategory(productDTO.getCategory());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }
        if (productDTO.getQuantity() >= 0) {
            product.setQuantity(productDTO.getQuantity());
        }
        if (productDTO.getImageUrl() != null) {
            imageService.deleteImage(product.getDeleteImageUrl());
            imageService.uploadImage(productDTO.getImageUrl(), productDTO);
            product.setImageUrl(productDTO.getImageUrl());
            product.setDeleteImageUrl(productDTO.getDeleteImageUrl());
        }
        if (productDTO.getAttributes() != null) {
            product.setAttributes(productDTO.getAttributes());
        }
        productRepository.save(product);
    }

    @Transactional
    @Override
    public Page<ProductDTO> findProductByName(String name, Pageable pageable) {
        Page<Product> products = productRepository.findProductsByNameLikeIgnoreCase("%" + name + "%", pageable);
        return products.map(Product::toProductDTO);
    }


    @Transactional
    @Override
    public Page<ProductDTO> findByQuantityLessThan(Pageable pageable, int quantity) {
        Page<Product> products = productRepository.findByQuantityLessThan(pageable, quantity);
        return products.map(Product::toProductDTO);
    }

    @Transactional
    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(Product::toProductDTO);
    }

    @Transactional
    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product does not exist"));

        return product.toProductDTO();
    }

    @Transactional
    @Override
    public Page<ProductDTO> getProductsByCategory(ProductCategory category, Pageable pageable) {
        Page<Product> products = productRepository.findProductsByCategory(category, pageable);
        return products.map(Product::toProductDTO);

    }

    @Transactional
    @Override
    public void updateProductQuantity(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product does not exist"));
        if (product.getQuantity() > quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
        }else {
            throw new IllegalArgumentException("The quantity of product is less than required: " + product.getQuantity());
        }
    }

    @Transactional
    @Override
    public Page<ProductDTO> searchProductByAttributes(String category, Map<String, List<String>> filters,
                                                      Double minPrice, Double maxPrice, Pageable pageable) {
        ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
        Specification<Product> specification = ProductSpecification.filterByAttributes(productCategory, filters, minPrice, maxPrice);
        Page<Product> productDTOS = productRepository.findAll(specification,pageable);
        return productDTOS.map(Product::toProductDTO);
    }
}
