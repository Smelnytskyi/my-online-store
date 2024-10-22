package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.exceptions.UserNotFoundException;
import com.gmail.deniska1406sme.onlinestore.model.Employee;
import com.gmail.deniska1406sme.onlinestore.repositories.EmployeeRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import com.gmail.deniska1406sme.onlinestore.services.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    public void testGetEmployeeByEmailExistingEmployee() {
        String email = "test@gmail.com";
        Employee employee = new Employee();
        employee.setEmail(email);

        when(employeeRepository.findByEmail(email)).thenReturn(employee);

        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(email);

        assertNotNull(employeeDTO);
        assertEquals(email, employee.getEmail());
        verify(employeeRepository).findByEmail(email);
    }

    @Test
    public void testGetEmployeeByEmailNonExistingEmployee() {
        String email = "test@gmail.com";

        when(employeeRepository.findByEmail(email)).thenReturn(null);

        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(email);

        assertNull(employeeDTO);
        verify(employeeRepository).findByEmail(email);
    }

    @Test
    public void testAddEmployeeExistingEmployee() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@gmail.com");

        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.addNewEmployee(new EmployeeDTO(), userDTO);
        });

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    public void testAddNewEmployee() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@gmail.com");
        EmployeeDTO employee = new EmployeeDTO();

        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);

        employeeService.addNewEmployee(employee, userDTO);

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployeeExistingEmployee() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName("Denys");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("Denis");

        employeeService.updateEmployee(employeeDTO, id);

        assertEquals("Denis", employee.getFirstName());
        verify(employeeRepository).findById(id);
        verify(employeeRepository).save(employee);
    }

    @Test
    public void testUpdateEmployeeNonExistingEmployee() {
        Long id = 1L;

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            employeeService.updateEmployee(new EmployeeDTO(), id);
        });

        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository).findById(id);
    }

    @Test
    public void testDeleteEmployeeExistingEmployee() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        employeeService.removeEmployee(new EmployeeDTO(id, "", "", ""));

        verify(employeeRepository).delete(employee);
    }

    @Test
    public void testDeleteEmployeeNonExistingEmployee() {
        Long id = 1L;

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            employeeService.removeEmployee(new EmployeeDTO(id, "", "", ""));
        });

        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository).findById(id);
    }


}
