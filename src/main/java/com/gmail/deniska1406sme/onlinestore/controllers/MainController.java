package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.config.JwtTokenProvider;
import com.gmail.deniska1406sme.onlinestore.dto.*;
import com.gmail.deniska1406sme.onlinestore.model.ProductCategory;
import com.gmail.deniska1406sme.onlinestore.services.*;
import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/main")
public class MainController {

    private final ProductService productService;
    private final ClientService clientService;
    private final CartService cartService;
    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailNotificationService emailNotificationService;
    private final PasswordAuthenticationService passwordAuthenticationService;
    long temp = 1L; //for temporary clients

    @Autowired
    public MainController(ProductService productService, ClientService clientService, CartService cartService,
                          OrderService orderService, JwtTokenProvider jwtTokenProvider,
                          EmailNotificationService emailNotificationService,
                          PasswordAuthenticationService passwordAuthenticationService) {
        this.productService = productService;
        this.clientService = clientService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailNotificationService = emailNotificationService;
        this.passwordAuthenticationService = passwordAuthenticationService;
    }

    @Cacheable("products")
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> getProducts(Pageable pageable,
                                                              @RequestParam(required = false, defaultValue = "name,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort sortObject = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObject);
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
            String email = jwtTokenProvider.getLogin(token);
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

    @GetMapping("/cart/count")
    public ResponseEntity<Map<String,Integer>> getCartCount(@RequestHeader(value = "Authorization", required = false) String token,
                                                            HttpSession session){
        ClientDTO clientDTO = getClientDTO(token, session);
        Set<CartItemDTO> cartItemDTOS = cartService.getCartItems(clientDTO);
        int count = 0;
        for(CartItemDTO cartItemDTO : cartItemDTOS){
            count += cartItemDTO.getQuantity();
        }
        Map<String,Integer> map = new HashMap<>();
        map.put("count", count);
        return ResponseEntity.ok(map);
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

    @CacheEvict(value = "products", allEntries = true)
    @PostMapping("/order/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestHeader("Authorization") String token, HttpSession session,
                                                @RequestBody(required = false) String notes) {
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
        try {
            for (CartItemDTO cartItemDTO : clientDTO.getCartDTO().getItems()) {
                productService.updateProductQuantity(cartItemDTO.getProductId(), cartItemDTO.getQuantity());
            }
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        OrderDTO order = orderService.addOrder(deliveryAddress, notes, clientDTO, clientDTO.getCartDTO().getItems());
        sendOrderConfirmationEmail(clientDTO, email);
        cartService.removeAllProductsFromCart(clientDTO);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchByName(@RequestParam String name, Pageable pageable) {
        Page<ProductDTO> productDTOS = productService.findProductByName(name, pageable);

        if (productDTOS.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(productDTOS);
    }

    @Cacheable("attributes")
    @GetMapping("/get-product-attributes")
    public ResponseEntity<Map<String, Set<String>>> getProductAttributes(@RequestParam String category) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Map<String, Set<String>> attributes = new TreeMap<>();
        List<ProductDTO> productDTOS = productService.getProductsByCategory(ProductCategory.valueOf(category.toUpperCase()), pageable).getContent();

        for (ProductDTO productDTO : productDTOS) {
            Map<String, String> attribute = productDTO.getAttributes();

            for (Map.Entry<String, String> entry : attribute.entrySet()) {
                String attributeName = entry.getKey();
                String attributeValue = entry.getValue();

                attributes.computeIfAbsent(attributeName, k -> new TreeSet<>()).add(attributeValue);
            }
        }
        return ResponseEntity.ok(attributes);
    }

    @PostMapping("/search-by-attributes")
    public ResponseEntity<Page<ProductDTO>> searchByAttributes(
            @RequestParam String category,
            @RequestParam(required = false, defaultValue = "0") Double minPrice,
            @RequestParam(required = false, defaultValue = "1.7976931348623157E308") Double maxPrice,
            @RequestBody Map<String, List<String>> filters,
            Pageable pageable) {

        Page<ProductDTO> filteredProducts = productService.searchProductByAttributes(category, filters, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(filteredProducts);
    }

    @GetMapping("/products-by-category")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(@RequestParam String category,
                                                                  @RequestParam(required = false, defaultValue = "name,asc") String sort,
                                                                  Pageable pageable) {
        String[] sortParams = sort.split(",");
        Sort sortObject = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObject);
        Page<ProductDTO> productDTOS = productService.getProductsByCategory(ProductCategory.valueOf(category.toUpperCase()), pageable);
        return ResponseEntity.ok(productDTOS);
    }

    @PostMapping("/registration")
    public ResponseEntity<Void> registerNewClient(@RequestBody @Validated(OnCreate.class) AddClientRequest request,
                                                  BindingResult bindingResult) {
        clientService.addNewClient(request.getClientDTO(), request.getUserDTO());
        passwordAuthenticationService.savePassword(request.getUserDTO().getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    private ClientDTO getClientDTO(String token, HttpSession session) {
        ClientDTO clientDTO;

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getLogin(token);
            clientDTO = clientService.getClientByEmail(email);
        } else {
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
        StringBuilder text = new StringBuilder("Ваш заказ успешно оформлен!\n\n");
        CartDTO cartDTO = clientDTO.getCartDTO();
        double totalPrice = 0.0;

        text.append("Состав заказа:\n");
        for (CartItemDTO item : cartDTO.getItems()) {
            ProductDTO productDTO = productService.getProductById(item.getProductId());
            double itemPrice = productDTO.getPrice() * item.getQuantity();
            totalPrice += itemPrice;

            text.append("— Товар: ").append(productDTO.getName())
                    .append("\n   Количество: ").append(item.getQuantity())
                    .append("\n   Цена за единицу: ").append(productDTO.getPrice()).append(" грн")
                    .append("\n   Общая стоимость: ").append(itemPrice).append(" грн\n\n");
        }
        cartDTO.setTotalPrice(totalPrice);

        text.append("Итоговая стоимость заказа: ").append(cartDTO.getTotalPrice()).append(" грн");
        return text.toString();
    }

    private void setRedirectAfterLogin(HttpSession session, String redirectUrl) {
        session.setAttribute("redirectAfterLogin", redirectUrl);
    }
}
