package com.gmail.deniska1406sme.onlinestore.repositories;

import com.gmail.deniska1406sme.onlinestore.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Employee findByEmail(String email);
}
