package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.OrderDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.OrderNotFoundException;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Client;
import com.gmail.deniska1406sme.onlinestore.model.Order;
import com.gmail.deniska1406sme.onlinestore.model.OrderItem;
import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import com.gmail.deniska1406sme.onlinestore.repositories.ClientRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ClientRepository clientRepository) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    @Override
    public OrderDTO addOrder(String deliveryAddress, String notes, ClientDTO clientDTO, Set<CartItemDTO> cartItems) {
        Order order = new Order();
        Client client = clientRepository.findById(clientDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("Client not found"));
        client.getOrders().add(order);
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setDeliveryAddress(deliveryAddress);
        order.setNotes(notes);
        for (CartItemDTO item : cartItems) {
            order.getOrderItems().add(new OrderItem(order, item.getProductId(), item.getQuantity()));
        }
        Order savedOrder = orderRepository.save(order);
        return savedOrder.toOrderDTO();
    }

    @Transactional
    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO, Long id) {
        Order order = orderRepository.findOrderById(id);
        if (order != null) {
            if (orderDTO.getOrderStatus() != null) {
                order.setOrderStatus(orderDTO.getOrderStatus());
            }
            if (orderDTO.getDeliveryAddress() != null) {
                order.setDeliveryAddress(orderDTO.getDeliveryAddress());
            }
            if (orderDTO.getNotes() != null) {
                order.setNotes(orderDTO.getNotes());
            }
            Order savedOrder = orderRepository.save(order);
            return savedOrder.toOrderDTO();
        } else {
            throw new OrderNotFoundException("Order not found");
        }
    }

    @Transactional
    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findOrderById(id);
        if (order != null) {
            orderRepository.delete(order);
        } else {
            throw new OrderNotFoundException("Order not found");
        }
    }

    @Transactional
    @Override
    public OrderDTO getOrder(Long id) {
        Order order = orderRepository.findOrderById(id);
        if (order != null) {
            return order.toOrderDTO();
        } else {
            throw new OrderNotFoundException("Order not found");
        }
    }

    @Transactional
    @Override
    public Page<OrderDTO> getOrdersByClient(Long id, Pageable pageable) {
        Page<Order> orders = orderRepository.findByClientId(id, pageable);
        return orders.map(Order::toOrderDTO);
    }

    @Transactional
    @Override
    public Page<OrderDTO> getOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(Order::toOrderDTO);
    }

    @Transactional
    @Override
    public Page<OrderDTO> getOrdersByStatus(OrderStatus orderStatus, Pageable pageable) {
        Page<Order> orders = orderRepository.findByOrderStatus(orderStatus, pageable);
        return orders.map(Order::toOrderDTO);
    }

    @Transactional
    @Override
    public ClientDTO getClient(Long id){
        Order order = orderRepository.findOrderById(id);
        Client client = order.getClient();
        return client.toClientDTO();
    }
}
