package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.config.JwtTokenProvider;
import com.gmail.deniska1406sme.onlinestore.dto.*;
import com.gmail.deniska1406sme.onlinestore.services.*;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/main")
public class MainController {

    private final ProductService productService;
    private final ClientService clientService;
    private final CartService cartService;
    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailNotificationService emailNotificationService;
    long temp = 1L; //for temporary clients

    @Autowired
    public MainController(ProductService productService, ClientService clientService, CartService cartService,
                          OrderService orderService, JwtTokenProvider jwtTokenProvider,
                          EmailNotificationService emailNotificationService) {
        this.productService = productService;
        this.clientService = clientService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailNotificationService = emailNotificationService;
    }

    @Cacheable("products")
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> getProducts(Pageable pageable) {
        Page<ProductDTO> productDTOS = productService.getAllProducts(pageable);
        return ResponseEntity.ok(productDTOS);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO productDTO = productService.getProductById(id);
        return ResponseEntity.ok(productDTO);
    }

    @PostMapping("/cart/add")
    public ResponseEntity<Void> addProductToCart(@RequestHeader(value = "Authorization", required = false) String token,
                                                 HttpSession session,
                                                 @RequestBody @Validated(OnUpdate.class) CartItemDTO cartItemDTO,
                                                 BindingResult bindingResult) {
        ClientDTO clientDTO;

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getLogin(token.replace("Bearer ", ""));
            clientDTO = clientService.getClientByEmail(email);
        } else {
            Long tempClientId = (Long) session.getAttribute("tempClientId");
            if (tempClientId != null) {
                clientDTO = clientService.getClientById(tempClientId);
            } else {
                clientDTO = clientService.createTemporaryClient(temp);
                temp += 1L;
                session.setAttribute("tempClientId", clientDTO.getId());
            }
        }
        cartService.addProductToCart(clientDTO, cartItemDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cart")
    public ResponseEntity<Set<CartItemDTO>> getCart(@RequestHeader(value = "Authorization", required = false) String token,
                                                    HttpSession session) {
        ClientDTO clientDTO = getClientDTO(token, session);
        Set<CartItemDTO> cartItemDTOS = cartService.getCartItems(clientDTO);
        return ResponseEntity.ok(cartItemDTOS);
    }

    @PutMapping("/cart/update")
    public ResponseEntity<Void> updateProductQuantity(@RequestHeader(value = "Authorization", required = false) String token,
                                                      HttpSession session,
                                                      @RequestBody @Validated(OnUpdate.class) CartItemDTO cartItemDTO,
                                                      BindingResult bindingResult) {
        ClientDTO clientDTO = getClientDTO(token, session);
        cartService.updateProductQuantity(clientDTO, cartItemDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/remove/{id}")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable Long id,
                                                      @RequestHeader(value = "Authorization", required = false) String token,
                                                      HttpSession session) {

        ClientDTO clientDTO = getClientDTO(token, session);
        cartService.removeProductFromCart(clientDTO, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestHeader("Authorization") String token, HttpSession session) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            setRedirectAfterLogin(session, "/order/create");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = jwtTokenProvider.getLogin(token);
        ClientDTO clientDTO = clientService.getClientByEmail(email);

        String deliveryAddress = clientDTO.getAddress();
        if (deliveryAddress == null || deliveryAddress.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        }

        String notes = "";//TODO: think about the logic of adding notes.

        for (CartItemDTO cartItemDTO: clientDTO.getCartDTO().getItems()){
            productService.updateProductQuantity(cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        }
        OrderDTO order = orderService.addOrder(deliveryAddress, notes, clientDTO);
        sendOrderConfirmationEmail(clientDTO, email);
        cartService.removeAllProductsFromCart(clientDTO);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchByName(@RequestParam String name, Pageable pageable){
        Page<ProductDTO> productDTOS = productService.findProductByName(name, pageable);

        if(productDTOS.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(productDTOS);
    }

    @GetMapping("/search-by-filter")
    public ResponseEntity<Page<ProductDTO>> searchByFilter(Pageable pageable, @RequestBody @Valid ProductFilterDTO filterDTO,
                                                           BindingResult bindingResult){
        Page<ProductDTO> productDTOS = productService.findFilteredProducts(pageable,filterDTO);
        if(productDTOS.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productDTOS);
    }


    private ClientDTO getClientDTO(String token, HttpSession session){
        ClientDTO clientDTO;

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getLogin(token.replace("Bearer ", ""));
            clientDTO = clientService.getClientByEmail(email);
        }else {
            Long tempClientId = (Long) session.getAttribute("tempClientId");
            clientDTO = clientService.getClientById(tempClientId);
        }
        return clientDTO;
    }

    private void sendOrderConfirmationEmail(ClientDTO clientDTO, String email) {
        String subject = "Подтверждение заказа";
        String text = generateOrderConfirmationText(clientDTO);

        emailNotificationService.sendOrderConfirmationEmail(email, subject, text);
    }

    private String generateOrderConfirmationText(ClientDTO clientDTO) {
        StringBuilder text = new StringBuilder("Ваш заказ успешно оформлен:\n");

        CartDTO cartDTO = clientDTO.getCartDTO();
        double totalPrice = 0.0;

        for (CartItemDTO item : cartDTO.getItems()) {
            ProductDTO productDTO = productService.getProductById(item.getProductId());

            double itemPrice = productDTO.getPrice() * item.getQuantity();
            totalPrice += itemPrice;

            text.append("Товар: ").append(productDTO.getName())
                    .append(", Количество: ").append(item.getQuantity())
                    .append(", Цена за единицу: ").append(productDTO.getPrice()).append(" грн")
                    .append(", Общая стоимость за товар: ").append(itemPrice).append(" грн\n");
        }
        cartDTO.setTotalPrice(totalPrice);

        text.append("\nОбщая стоимость заказа: ").append(cartDTO.getTotalPrice()).append(" грн");
        return text.toString();
    }

    private void setRedirectAfterLogin(HttpSession session, String redirectUrl){
        session.setAttribute("redirectAfterLogin", redirectUrl);
    }

    //TODO: add filter found
}
