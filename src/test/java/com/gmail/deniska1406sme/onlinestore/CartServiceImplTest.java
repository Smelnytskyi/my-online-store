package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.ProductNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Cart;
import com.gmail.deniska1406sme.onlinestore.model.CartItem;
import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.repositories.CartItemRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.CartRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import com.gmail.deniska1406sme.onlinestore.services.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    public void testAddProductToCartNewItem() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>());

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(100.0);

        CartItemDTO cartItemDTO = new CartItemDTO(1L, 1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.addProductToCart(clientDTO, cartItemDTO);

        assertEquals(1, cart.getItems().size());
        CartItem addedItem = cart.getItems().iterator().next();
        assertEquals(1L, addedItem.getProduct().getId());
        assertEquals(1, addedItem.getQuantity());

        verify(cartRepository).save(cart);
        verify(cartItemRepository).save(addedItem);
    }

    @Test
    public void testAddProductToCartExistingItem() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Product product = new Product();
        product.setId(1L);

        CartItem existingItem = new CartItem(1, product);

        Cart cart = new Cart();
        cart.setItems(Set.of(existingItem));

        CartItemDTO cartItemDTO = new CartItemDTO(1L, 2);

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        cartService.addProductToCart(clientDTO, cartItemDTO);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, existingItem.getQuantity());
    }

    @Test
    public void testAddProductToCartProductNotFound() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>());

        when(cartRepository.findByClientId(1L)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        CartItemDTO cartItemDTO = new CartItemDTO(1L, 1);

        assertThrows(ProductNotFoundException.class, () -> cartService.addProductToCart(clientDTO, cartItemDTO));
    }

    @Test
    public void testRemoveProductFromCartExistingItem() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Product product = new Product();
        product.setId(1L);

        CartItem existingItem = new CartItem(1, product);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>());
        cart.getItems().add(existingItem);

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        cartService.removeProductFromCart(clientDTO, product.getId());

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    public void testRemoveProductFromCartProductNotFound() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>());

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        assertThrows(ProductNotFoundException.class, () -> cartService.removeProductFromCart(clientDTO, 1L));
    }

    @Test
    public void testRemoveAllProductsFromCart() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>(Set.of(new CartItem(1, new Product()))));

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        cartService.removeAllProductsFromCart(clientDTO);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    public void testUpdateProductQuantity() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Product product = new Product();
        product.setId(1L);

        CartItem existingItem = new CartItem(1, product);
        existingItem.setQuantity(3);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>(Set.of(existingItem)));

        CartItemDTO cartItemDTO = new CartItemDTO(1L, 5);

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        cartService.updateProductQuantity(clientDTO, cartItemDTO);

        assertEquals(5, existingItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    public void testUpdateProductQuantityProductNotFound() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>());

        CartItemDTO cartItemDTO = new CartItemDTO(1L, 5);

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        assertThrows(ProductNotFoundException.class, () -> cartService.updateProductQuantity(clientDTO, cartItemDTO));
    }

    @Test
    public void testGetTotalPrice() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Product product = new Product();
        product.setPrice(100.0);
        CartItem cartItem = new CartItem(2, product);

        Product product2 = new Product();
        product2.setPrice(50.0);
        CartItem cartItem2 = new CartItem(1, product2);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>(Set.of(cartItem, cartItem2)));

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        Double totalPrice = cartService.getTotalPrice(clientDTO);

        assertEquals(250.0, totalPrice);
    }

    @Test
    public void testGetCartItems() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Product product = new Product();
        product.setId(1L);

        CartItem cartItem = new CartItem(1, product);
        Cart cart = new Cart();
        cart.setItems(new HashSet<>(Set.of(cartItem)));

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        Set<CartItemDTO> cartItems = cartService.getCartItems(clientDTO);

        assertNotNull(cartItems);
        assertEquals(1, cartItems.size());
        assertEquals(1L, cartItems.iterator().next().getProductId());
    }

    @Test
    public void testGetCartItemsEmptyCart() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new HashSet<>());

        when(cartRepository.findByClientId(1L)).thenReturn(cart);

        Set<CartItemDTO> cartItems = cartService.getCartItems(clientDTO);

        assertNotNull(cartItems);
        assertTrue(cartItems.isEmpty());
    }

    @Test
    public void testTransferCartToClient() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(1L);

        ClientDTO temporary = new ClientDTO();
        temporary.setId(2L);

        Product product = new Product();
        product.setId(1L);

        Cart temporaryCart = new Cart();
        temporaryCart.setItems(new HashSet<>(Set.of(new CartItem(1, product))));

        when(cartRepository.findByClientId(temporary.getId())).thenReturn(temporaryCart);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        Cart clientCart = new Cart();
        clientCart.setItems(new HashSet<>());
        when(cartRepository.findByClientId(clientDTO.getId())).thenReturn(clientCart);

        cartService.transferCartToClient(clientDTO, temporary);

        Set<CartItemDTO> cartItems = cartService.getCartItems(clientDTO);
        assertEquals(1, cartItems.size());
    }

}
