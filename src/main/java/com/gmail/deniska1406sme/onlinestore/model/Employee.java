package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.EmployeeDTO;
import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Employee extends User {
    @Column(nullable = false, length = 30)
    private String firstName;

    @Column(nullable = false, length = 30)
    private String lastName;

    @Column(unique = true, nullable = false, length = 13)
    private String phone;

    public Employee() {
    }

    public Employee(String email, String password, String googleId, String firstName, String lastName, String phone) {
        super(email, password, googleId, UserRole.EMPLOYEE);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public static Employee of(String email, String password, String googleId, String firstName, String lastName,
                              String phone) {
        return new Employee(email, password, googleId, firstName, lastName, phone);
    }

    public EmployeeDTO toEmployeeDTO() {
        UserDTO userDTO = super.toUserDTO();
        return EmployeeDTO.of(userDTO.getId(), firstName, lastName, phone);
    }

    public static Employee fromDTO(UserDTO userDTO, EmployeeDTO employeeDTO) {
        return Employee.of(userDTO.getEmail(), null, userDTO.getGoogleId(), employeeDTO.getFirstName(),
                employeeDTO.getLastName(), employeeDTO.getPhone());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + getId() +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
