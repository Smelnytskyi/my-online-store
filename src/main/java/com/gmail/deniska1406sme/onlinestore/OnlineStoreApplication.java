package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.CartItemDTO;
import com.gmail.deniska1406sme.onlinestore.model.*;
import com.gmail.deniska1406sme.onlinestore.repositories.*;
import com.gmail.deniska1406sme.onlinestore.services.PasswordAuthenticationService;
import com.gmail.deniska1406sme.onlinestore.services.ProductImporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@EnableCaching
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
@SpringBootApplication
public class OnlineStoreApplication {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordAuthenticationService passwordAuthenticationService;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private ProductImporterService productImporterService;

    public static void main(String[] args) {
        SpringApplication.run(OnlineStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ClientRepository clientRepository, OrderRepository orderRepository) {
        return args -> {

            String[] filePaths = {
                    "src/main/resources/static/products/CORPS.txt",
                    "src/main/resources/static/products/MOTHERBOARD.txt",
                    "src/main/resources/static/products/CPU.txt",
                    "src/main/resources/static/products/GPU.txt",
                    "src/main/resources/static/products/HDD.txt",
                    "src/main/resources/static/products/OTHER.txt",
                    "src/main/resources/static/products/POWER_UNIT.txt",
                    "src/main/resources/static/products/RAM.txt",
                    "src/main/resources/static/products/SSD.txt",
                    "src/main/resources/static/products/REFRIGERATOR_SYSTEM.txt",
            };

            for (String filePath : filePaths) {
                productImporterService.importProductsFromFile(filePath);
            }


            Employee employee1 = new Employee("emp1@gmail.com", "pass", "", "Denys", "Smel", "380094");
            employeeRepository.save(employee1);
            Employee employee2 = new Employee("emp2@gmail.com", "pass", "", "Denys2", "Smel2", "3800942");
            employeeRepository.save(employee2);

            passwordAuthenticationService.savePassword("emp1@gmail.com", "password");

            Client client1 = new Client("client1@gmail.com", UserRole.CLIENT, "Denys", "Smel", "12548");
            clientRepository.save(client1);

            Client testMail = new Client("denys.smelnytskyi@nure.ua", null, null,
                    "Denys", "Smel", "Main st.15", "32432525");
            clientRepository.save(testMail);

            Set<CartItemDTO> cartItemDTOS = new HashSet<>();
            CartItemDTO cartItemDTO = new CartItemDTO(1L, 3);
            cartItemDTOS.add(cartItemDTO);

            Order order1 = new Order(client1, LocalDateTime.now(), OrderStatus.SHIPPED, "my street 22", "i dont need instruction",
                    cartItemDTOS);
            orderRepository.save(order1);

            Order order2 = new Order(testMail, LocalDateTime.now(), OrderStatus.CONFIRMED, "my street 25", "i dont need instruction",
                    cartItemDTOS);
            orderRepository.save(order2);

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

            Admin admin = new Admin("admin@gmail.com", UserRole.ADMIN);
            adminRepository.save(admin);
            passwordAuthenticationService.savePassword("admin@gmail.com", "password");
        };
    }

}
