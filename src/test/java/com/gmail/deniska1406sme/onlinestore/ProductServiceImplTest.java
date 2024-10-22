package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.ProductNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import com.gmail.deniska1406sme.onlinestore.services.ImageService;
import com.gmail.deniska1406sme.onlinestore.services.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setCategory(ProductCategory.CPU);
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setQuantity(10);
        product.setImageUrl("testImageUrl");
        product.setDeleteImageUrl("testDeleteImageUrl");

        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setCategory(ProductCategory.CPU);
        productDTO.setDescription("Test Description");
        productDTO.setPrice(100.0);
        productDTO.setQuantity(10);
        productDTO.setImageUrl("testImageUrl");
        productDTO.setDeleteImageUrl("testDeleteImageUrl");
    }

    @Test
    public void testAddProductSuccess() {

        when(productRepository.existsByName(productDTO.getName())).thenReturn(false);

        productService.addProduct(productDTO);

        verify(imageService).uploadImage(productDTO.getImageUrl(), productDTO);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void testAddProductExistingProduct() {
        when(productRepository.existsByName(productDTO.getName())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(productDTO));

        verify(productRepository).existsByName(productDTO.getName());
        verify(imageService, never()).uploadImage(anyString(), any(ProductDTO.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testRemoveProductSuccess() {

        when(productRepository.existsById(product.getId())).thenReturn(true);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.removeProduct(product.getId());

        verify(imageService).deleteImage(product.getDeleteImageUrl());
        verify(productRepository).deleteById(product.getId());
    }

    @Test
    public void testRemoveProductNonExistingProduct() {
        when(productRepository.existsById(product.getId())).thenReturn(false);
        assertThrows(ProductNotFoundException.class, () -> productService.removeProduct(product.getId()));

        verify(productRepository).existsById(product.getId());
        verify(imageService, never()).deleteImage(anyString());
        verify(productRepository, never()).deleteById(product.getId());
    }

    @Test
    public void testUpdateProductSuccess() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.updateProduct(productDTO, product.getId());

        verify(imageService).uploadImage(productDTO.getImageUrl(), productDTO);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void testUpdateProductNotFound() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productDTO, product.getId()));

        verify(productRepository).findById(product.getId());
        verify(imageService, never()).deleteImage(anyString());
        verify(productRepository, never()).deleteById(product.getId());
    }

    @Test
    public void testGetProductByIdSuccess() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ProductDTO res = productService.getProductById(product.getId());

        assertNotNull(res);
        assertEquals(product.getName(), res.getName());
        verify(productRepository).findById(product.getId());
    }

    @Test
    public void testGetProductByIdNotFound() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(product.getId()));

        verify(productRepository).findById(product.getId());
    }
}
