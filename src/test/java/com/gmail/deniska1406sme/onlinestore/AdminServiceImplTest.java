package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Employee;
import com.gmail.deniska1406sme.onlinestore.repositories.EmployeeRepository;
import com.gmail.deniska1406sme.onlinestore.services.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    public void testGetEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = List.of(new Employee("denys@mail.com", "pass", null, "Denys", "Smel", "3950543435"));
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        Page<EmployeeDTO> res = adminService.getEmployees(pageable);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals("Denys", res.getContent().get(0).getFirstName());
    }

    @Test
    public void testGetEmployeeById() {
        Employee employee = new Employee("denys@mail.com", "pass", null, "Denys", "Smel", "3950543435");
        employee.setId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeDTO res = adminService.getEmployeeById(1L);

        assertNotNull(res);
        assertEquals("Denys", res.getFirstName());
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.getEmployeeById(1L));
    }
}
