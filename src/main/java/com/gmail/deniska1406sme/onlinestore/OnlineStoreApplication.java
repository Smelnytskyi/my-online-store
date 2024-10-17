package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.model.*;
import com.gmail.deniska1406sme.onlinestore.repositories.ClientRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.EmployeeRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.OrderRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import com.gmail.deniska1406sme.onlinestore.services.ImageService;
import com.gmail.deniska1406sme.onlinestore.services.PasswordAuthenticationService;
import com.gmail.deniska1406sme.onlinestore.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EnableCaching
@SpringBootApplication
public class OnlineStoreApplication {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private PasswordAuthenticationService passwordAuthenticationService;

    public static void main(String[] args) {
        SpringApplication.run(OnlineStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ClientRepository clientRepository, OrderRepository orderRepository) {
        return args -> {
            ImageUtil imageUtil = new ImageUtil();


            Map<String, String> atributes = new HashMap<>();
            atributes.put("Company", "Intel");
            atributes.put("Connector type", "Socket 1700");
            atributes.put("Base clock frequency", "2500");

            Product product1 = new Product();
            product1.setName("Товар 1");
            product1.setPrice(100.0);
            product1.setCategory(ProductCategory.CPU);
            product1.setDescription("description");
            product1.setQuantity(7);
            product1.setImageUrl("");
            product1.setDeleteImageUrl("");
            product1.setAttributes(atributes);
            productRepository.save(product1);

            ProductDTO productDTO = product1.toProductDTO();
            String photo1 = imageUtil.convertImageToBase64("C:\\Users\\smede\\Desktop\\proga hw\\OnlineStore" +
                    "\\src\\main\\resources\\static\\productPhotos\\photo 1.jpg");
            imageService.uploadImage(photo1, productDTO);
            product1.setImageUrl(productDTO.getImageUrl());
            product1.setDeleteImageUrl(productDTO.getDeleteImageUrl());
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("Товар 2");
            product2.setPrice(200.0);
            product2.setCategory(ProductCategory.HDD);
            product2.setDescription("description");
            product2.setQuantity(5);
            product2.setImageUrl("");
            product2.setDeleteImageUrl("");
            productRepository.save(product2);

            Employee employee1 = new Employee("emp1@gmail.com", "pass", "", "Denys", "Smel", "380094");
            employeeRepository.save(employee1);
            Employee employee2 = new Employee("emp2@gmail.com", "pass", "", "Denys2", "Smel2", "3800942");
            employeeRepository.save(employee2);

            Client client1 = new Client("client1@gmail.com", UserRole.CLIENT, "Denys", "Smel", "12548");
            clientRepository.save(client1);
            Order order1 = new Order(client1, LocalDateTime.now(), OrderStatus.SHIPPED, "my street 25", "i dont need instruction");
            orderRepository.save(order1);

            Client testMail = new Client("denys.smelnytskyi@nure.ua", null, null,
                    "Denys", "Smel", "Main st.15", "32432525");
            clientRepository.save(testMail);

            Cart newCart = new Cart();
            newCart.setItems(new HashSet<>());
            newCart.setTotalPrice(0.0);
            Set<Order> orders = new HashSet<>();
            testMail.setCart(newCart);
            testMail.setOrders(orders);
            newCart.setClient(testMail);
            for (Order order : orders) {
                order.setClient(testMail);
            }
            clientRepository.save(testMail);

            passwordAuthenticationService.savePassword("denys.smelnytskyi@nure.ua", "password");
        };
    }

}
