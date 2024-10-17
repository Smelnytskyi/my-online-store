package com.gmail.deniska1406sme.onlinestore.repositories;

import com.gmail.deniska1406sme.onlinestore.model.Cart;
import com.gmail.deniska1406sme.onlinestore.model.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);

    Page<CartItem> findByCart(Cart cart, Pageable pageable);

}
