package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.dto.OrderDTO;
import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.model.OrderStatus;
import com.gmail.deniska1406sme.onlinestore.services.OrderService;
import com.gmail.deniska1406sme.onlinestore.services.ProductService;
import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @DeleteMapping("/order/delete{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/get-single/{id}")
    public ResponseEntity<OrderDTO> getSingleOrder(@PathVariable Long id) {
        OrderDTO orderDTO = orderService.getOrder(id);
        return ResponseEntity.ok(orderDTO);
    }

    @PostMapping("/product/add")
    public ResponseEntity<Void> addProduct(@RequestBody @Validated(OnCreate.class) ProductDTO productDTO,
                                           BindingResult bindingResult) {
        productService.addProduct(productDTO);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/product/update/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id,
                                              @RequestBody @Validated(OnUpdate.class) ProductDTO productDTO,
                                              BindingResult bindingResult) {
        productService.updateProduct(productDTO, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/product/delete/{id}")
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
