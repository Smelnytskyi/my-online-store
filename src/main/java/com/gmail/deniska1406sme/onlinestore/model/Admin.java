package com.gmail.deniska1406sme.onlinestore.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    private String firstName = "admin";
    private String lastName = "admin";
    private String phone = "123456789";

    public Admin() { //TODO: create only one account of admin
        super();
    }

    public Admin(String email, UserRole role) {
        super(email, role);
    }
}
