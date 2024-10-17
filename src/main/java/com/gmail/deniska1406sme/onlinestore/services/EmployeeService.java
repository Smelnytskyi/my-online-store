package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;

public interface EmployeeService {

    EmployeeDTO getEmployeeByEmail(String email);

    void addNewEmployee(EmployeeDTO employeeDTO, UserDTO userDTO);

    void updateEmployee(EmployeeDTO employeeDTO, Long id);

    void removeEmployee(EmployeeDTO employeeDTO);
}
