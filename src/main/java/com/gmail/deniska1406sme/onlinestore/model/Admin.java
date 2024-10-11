package com.gmail.deniska1406sme.onlinestore.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    public Admin() { //TODO: create only one account of admin
        super();
    }
}
