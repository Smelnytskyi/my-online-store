package com.gmail.deniska1406sme.onlinestore.model;

public enum UserRole {
    ADMIN, CLIENT, EMPLOYEE, TEMPORARY;

    @Override
    public String toString() {
        return name();
    }
}
