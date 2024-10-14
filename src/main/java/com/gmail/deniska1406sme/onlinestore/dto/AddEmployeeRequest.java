package com.gmail.deniska1406sme.onlinestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class AddEmployeeRequest {
    @Valid
    private EmployeeDTO employee;
    @Valid
    private UserDTO user;
    @NotBlank
    private String password;

    public EmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDTO employee) {
        this.employee = employee;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
