package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.OrderDTO;
import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;


public interface OrderService {

    OrderDTO addOrder(String deliveryAddress, String notes, ClientDTO clientDTO, Set<CartItemDTO> cartItems);

    OrderDTO updateOrder(OrderDTO orderDTO, Long id);

    void deleteOrder(Long id);

    OrderDTO getOrder(Long id);

    Page<OrderDTO> getOrdersByClient(Long id, Pageable pageable);

    Page<OrderDTO> getOrders(Pageable pageable);

    Page<OrderDTO> getOrdersByStatus(OrderStatus orderStatus, Pageable pageable);

    ClientDTO getClient(Long id);

}
