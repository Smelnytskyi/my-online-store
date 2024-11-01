package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.OrderDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.OrderNotFoundException;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Client;
import com.gmail.deniska1406sme.onlinestore.model.Order;
import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import com.gmail.deniska1406sme.onlinestore.repositories.ClientRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.OrderRepository;
import com.gmail.deniska1406sme.onlinestore.services.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Client client;
    private Order order;

    @BeforeEach
    public void setUp() {
        client = new Client();
        client.setId(1L);
        client.setOrders(new HashSet<>());

        order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
    }

    @Test
    public void testAddOrderSuccess() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(id);

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Set<CartItemDTO> cartItemDTOS = new HashSet<>();
        CartItemDTO cartItemDTO = new CartItemDTO(1L, 3);
        cartItemDTOS.add(cartItemDTO);

        OrderDTO orderDTO = orderService.addOrder("123 Main St", "Test order", clientDTO, cartItemDTOS);

        assertNotNull(orderDTO);
        assertEquals(1L, orderDTO.getId());
        verify(clientRepository).findById(id);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    public void testAddOrderClientNotFound() {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(id);

        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        Set<CartItemDTO> cartItemDTOS = new HashSet<>();
        CartItemDTO cartItemDTO = new CartItemDTO(1L, 3);
        cartItemDTOS.add(cartItemDTO);

        assertThrows(UserNotFoundException.class, () -> {
            orderService.addOrder("123 Main St", "Test order", clientDTO, cartItemDTOS);
        });
        verify(clientRepository).findById(id);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testUpdateOrderSuccess() {
        Long id = 1L;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findOrderById(id)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO res = orderService.updateOrder(orderDTO, id);

        assertNotNull(res);
        assertEquals(order.getId(), res.getId());
        verify(orderRepository).findOrderById(id);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    public void testUpdateOrderNotFound() {
        Long id = 1L;
        OrderDTO orderDTO = new OrderDTO();

        when(orderRepository.findOrderById(id)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.updateOrder(orderDTO, id);
        });

        verify(orderRepository).findOrderById(id);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testDeleteOrderSuccess() {
        Long id = 1L;

        when(orderRepository.findOrderById(id)).thenReturn(order);

        orderService.deleteOrder(id);

        verify(orderRepository).findOrderById(id);
        verify(orderRepository).delete(order);
    }

    @Test
    public void testDeleteOrderNotFound() {
        Long id = 1L;

        when(orderRepository.findOrderById(id)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.deleteOrder(id);
        });

        verify(orderRepository).findOrderById(id);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    public void testGetOrderSuccess() {
        Long id = 1L;

        when(orderRepository.findOrderById(id)).thenReturn(order);

        OrderDTO res = orderService.getOrder(id);

        assertNotNull(res);
        assertEquals(order.getId(), res.getId());
        verify(orderRepository).findOrderById(id);
    }

    @Test
    public void testGetOrderNotFound() {
        Long id = 1L;

        when(orderRepository.findOrderById(id)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrder(id);
        });

        verify(orderRepository).findOrderById(id);
    }
}
