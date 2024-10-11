package com.gmail.deniska1406sme.onlinestore.repositories;

import com.gmail.deniska1406sme.onlinestore.model.Order;
import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByClientId(Long clientId, Pageable pageable);
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
    Order findOrderById(Long orderId);


}
