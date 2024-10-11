package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import com.gmail.deniska1406sme.onlinestore.model.Employee;
import com.gmail.deniska1406sme.onlinestore.repositories.EmployeeRepository;
import com.gmail.deniska1406sme.onlinestore.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email);
        if (employee == null) {
            return null;
        }
        return employee.toEmployeeDTO();
    }

    @Transactional
    @Override
    public void addNewEmployee(EmployeeDTO employeeDTO, UserDTO userDTO) {
        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        Employee employee = Employee.fromDTO(userDTO, employeeDTO);
        employeeRepository.save(employee);
    }

    @Transactional
    @Override
    public void updateEmployee(EmployeeDTO employeeDTO, Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (employeeDTO.getFirstName() != null) {
            employee.setFirstName(employeeDTO.getFirstName());
        }
        if (employeeDTO.getLastName() != null) {
            employee.setLastName(employeeDTO.getLastName());
        }
        if (employeeDTO.getPhone() != null) {
            employee.setPhone(employeeDTO.getPhone());
        }
        employeeRepository.save(employee);
    }

    @Transactional
    @Override
    public void removeEmployee(EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(employeeDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employeeRepository.delete(employee);
    }
}
