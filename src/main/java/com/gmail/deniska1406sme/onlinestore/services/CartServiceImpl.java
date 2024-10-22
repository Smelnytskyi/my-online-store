package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.ProductNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Cart;
import com.gmail.deniska1406sme.onlinestore.model.CartItem;
import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.repositories.CartItemRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.CartRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    @Override
    public void addProductToCart(ClientDTO clientDTO, CartItemDTO cartItemDTO) {
        Cart cart = cartRepository.findByClientId(clientDTO.getId());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItemDTO.getProductId()))
                .findFirst();
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        } else {
            Product product = productRepository.findById(cartItemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            CartItem cartItem = new CartItem(cartItemDTO.getQuantity(), product);
            cartItemRepository.save(cartItem);
            cart.getItems().add(cartItem);
        }
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void removeProductFromCart(ClientDTO clientDTO, Long productId) {
        Cart cart = cartRepository.findByClientId(clientDTO.getId());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if (existingItem.isPresent()) {
            cart.getItems().remove(existingItem.get());
        } else {
            throw new ProductNotFoundException("Product not found in cart");
        }
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void removeAllProductsFromCart(ClientDTO clientDTO) {
        Cart cart = cartRepository.findByClientId(clientDTO.getId());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void updateProductQuantity(ClientDTO clientDTO, CartItemDTO cartItemDTO) {
        if (cartItemDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = cartRepository.findByClientId(clientDTO.getId());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItemDTO.getProductId()))
                .findFirst();
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItemDTO.getQuantity());
        } else {
            throw new ProductNotFoundException("Product not found in cart");
        }
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    public Double getTotalPrice(ClientDTO clientDTO) {
        Double totalPrice = 0.0;
        Cart cart = cartRepository.findByClientId(clientDTO.getId());
        Set<CartItem> cartItems = cart.getItems();
        for (CartItem cartItem : cartItems) {
            totalPrice += cartItem.getProduct().getPrice() * cartItem.getQuantity();
        }
        return totalPrice;
    }

    @Transactional
    @Override
    public Set<CartItemDTO> getCartItems(ClientDTO clientDTO) {
        if (clientDTO == null) {
            return Collections.emptySet();
        }
        Cart cart = cartRepository.findByClientId(clientDTO.getId());
        if (cart == null) {
            return Collections.emptySet();
        }

        Set<CartItem> cartItems = cart.getItems();
        Set<CartItemDTO> cartItemDTOs = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            cartItemDTOs.add(cartItem.toCartItemDTO());
        }
        return cartItemDTOs;
    }

    @Transactional
    @Override
    public void transferCartToClient(ClientDTO clientDTO, ClientDTO temporaryClientDTO) {
        Set<CartItemDTO> temporaryCartItems = getCartItems(temporaryClientDTO);
        for (CartItemDTO item : temporaryCartItems) {
            addProductToCart(clientDTO, item);
        }
    }

}
