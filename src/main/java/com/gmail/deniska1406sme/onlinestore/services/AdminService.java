package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<EmployeeDTO> getEmployees(Pageable pageable);

    EmployeeDTO getEmployeeById(Long id);

}
