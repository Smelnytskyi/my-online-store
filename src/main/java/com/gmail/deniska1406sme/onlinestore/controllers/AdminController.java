package com.gmail.deniska1406sme.onlinestore.controllers;

import com.gmail.deniska1406sme.onlinestore.dto.AddEmployeeRequest;
import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.services.AdminService;
import com.gmail.deniska1406sme.onlinestore.services.EmployeeService;
import com.gmail.deniska1406sme.onlinestore.services.PasswordAuthenticationService;
import com.gmail.deniska1406sme.onlinestore.services.UserService;
import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final PasswordAuthenticationService passwordAuthenticationService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService, EmployeeService employeeService,
                           PasswordAuthenticationService passwordAuthenticationService) {
        this.adminService = adminService;
        this.userService = userService;
        this.employeeService = employeeService;
        this.passwordAuthenticationService = passwordAuthenticationService;
    }

    @Cacheable("employees")
    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeeDTO>> getEmployees(Pageable pageable) {
        Page<EmployeeDTO> employees = adminService.getEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    @DeleteMapping("/user/delete/{userId}")
    @CacheEvict(value = "employees", allEntries = true)
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/delete")
    @CacheEvict(value = "employees", allEntries = true)
    public ResponseEntity<Void> deleteUsers(@RequestBody List<Long> userIds) {
        for (Long userId : userIds) {
            userService.deleteUser(userId);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/employee/add")
    @CacheEvict(value = "employees", allEntries = true)
    public ResponseEntity<List<String>> addEmployee(@RequestBody @Validated(OnCreate.class) AddEmployeeRequest request,
                                                    BindingResult bindingResult) {
        EmployeeDTO employeeDTO = request.getEmployee();
        UserDTO userDTO = request.getUser();
        String rawPassword = request.getPassword();
        employeeService.addNewEmployee(employeeDTO, userDTO);
        passwordAuthenticationService.savePassword(userDTO.getEmail(), rawPassword);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/employee/update/{id}")
    @CacheEvict(value = "employees", allEntries = true)
    public ResponseEntity<List<String>> updateEmployee(@PathVariable Long id,
                                                       @RequestBody @Validated(OnUpdate.class) EmployeeDTO employeeDTO,
                                                       BindingResult bindingResult) {
        employeeService.updateEmployee(employeeDTO, id);
        return ResponseEntity.ok().build();
    }
}
