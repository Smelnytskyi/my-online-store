package com.gmail.deniska1406sme.onlinestore.dto;

import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "First name must not be blank", groups = OnCreate.class)
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters", groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @NotBlank(message = "Last name must not be blank", groups = OnCreate.class)
    @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters", groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @NotBlank(message = "Phone must not be blank",groups = OnCreate.class)
    @Size(min = 10, max = 13, message = "Phone must be between 10 and 13 characters", groups = {OnCreate.class, OnUpdate.class})
    private String phone;

    public EmployeeDTO() {
    }

    public EmployeeDTO(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public EmployeeDTO(Long id, String firstName, String lastName, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public static EmployeeDTO of(String firstName, String lastName, String phone) {
        return new EmployeeDTO(null, firstName, lastName, phone);
    }

    public static EmployeeDTO of(Long id, String firstName, String lastName, String phone) {
        return new EmployeeDTO(id, firstName, lastName, phone);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "EmployeeDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
