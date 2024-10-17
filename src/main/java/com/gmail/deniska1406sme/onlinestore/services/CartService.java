package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;

import java.util.Set;

public interface CartService {

    void addProductToCart(ClientDTO clientDTO, CartItemDTO cartItemDTO);

    void removeProductFromCart(ClientDTO clientDTO, Long productId);

    void removeAllProductsFromCart(ClientDTO clientDTO);

    void updateProductQuantity(ClientDTO clientDTO, CartItemDTO cartItemDTO);

    Double getTotalPrice(ClientDTO clientDTO);

    Set<CartItemDTO> getCartItems(ClientDTO clientDTO);

    void transferCartToClient(ClientDTO clientDTO, ClientDTO temporaryClientDTO);
}
