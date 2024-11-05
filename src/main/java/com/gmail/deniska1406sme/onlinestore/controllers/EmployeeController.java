package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.dto.ClientDTO;
import com.gmail.deniska1406sme.onlinestore.dto.OrderDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.OrderNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import com.gmail.deniska1406sme.onlinestore.services.ClientService;
import com.gmail.deniska1406sme.onlinestore.services.OrderService;
import com.gmail.deniska1406sme.onlinestore.services.ProductService;
import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final OrderService orderService;
    private final ProductService productService;


    @Autowired
    public EmployeeController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDTO>> getOrders(Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrders(pageable);
        for(OrderDTO order: orders){
            ClientDTO clientDTO = orderService.getClient(order.getId());
            order.setClientFirstName(clientDTO.getFirstName());
            order.setClientLastName(clientDTO.getLastName());
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders-by-status")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(@RequestParam OrderStatus status, Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/order/update/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@RequestBody OrderDTO orderDTO, @PathVariable Long id) {
        OrderDTO updatedOrder = orderService.updateOrder(orderDTO, id);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/order/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/get-single/{id}")
    public ResponseEntity<OrderDTO> getSingleOrder(@PathVariable Long id) {
        try {
            OrderDTO orderDTO = orderService.getOrder(id);
            ClientDTO clientDTO = orderService.getClient(orderDTO.getId());
            orderDTO.setClientFirstName(clientDTO.getFirstName());
            orderDTO.setClientLastName(clientDTO.getLastName());
            return ResponseEntity.ok(orderDTO);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/product/add")
    @CacheEvict(value = {"products", "attributes"}, allEntries = true)
    public ResponseEntity<Void> addProduct(@RequestBody @Validated(OnCreate.class) ProductDTO productDTO,
                                           BindingResult bindingResult) {
        productService.addProduct(productDTO);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/product/update/{id}")
    @CacheEvict(value = {"products", "attributes"}, allEntries = true)
    public ResponseEntity<Void> updateProduct(@PathVariable Long id,
                                              @RequestBody @Validated(OnUpdate.class) ProductDTO productDTO,
                                              BindingResult bindingResult) {
        productService.updateProduct(productDTO, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/product/delete/{id}")
    @CacheEvict(value = {"products", "attributes"}, allEntries = true)
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.removeProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products-by-quantity")
    public ResponseEntity<Page<ProductDTO>> getProductsByQuantity(@RequestParam int quantity, Pageable pageable) {
        Page<ProductDTO> products = productService.findByQuantityLessThan(pageable, quantity);
        return ResponseEntity.ok(products);
    }
}
