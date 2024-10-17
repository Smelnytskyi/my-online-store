package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ProductFilterDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.ProductNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import com.gmail.deniska1406sme.onlinestore.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<ProductDTO> findFilteredProducts(Pageable pageable, ProductFilterDTO filterDTO) {
        Specification<Product> specification = Specification.where(null);

        if (filterDTO.getProductName() != null) {
            specification = specification.and(ProductSpecification.hasName(filterDTO.getProductName()));
        }
        if (filterDTO.getCategory() != null) {
            specification = specification.and(ProductSpecification.hasCategory(filterDTO.getCategory()));
        }
        if (filterDTO.getMinPrice() != null) {
            specification = specification.and(ProductSpecification.hasMinPrice(filterDTO.getMinPrice()));
        }
        if (filterDTO.getMaxPrice() != null) {
            specification = specification.and(ProductSpecification.hasMaxPrice(filterDTO.getMaxPrice()));
        }

        Page<Product> products = productRepository.findAll(specification, pageable);

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
    public void updateProductQuantity(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product does not exist"));
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }
}
