package com.gmail.deniska1406sme.onlinestore.model;

import com.gmail.deniska1406sme.onlinestore.dto.UserDTO;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String googleId;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false)
    private UserRole role;

    public User() {
    }

    public User(String email, UserRole role) {
        this.email = email;
        this.role = role;
    }

    public User(String email, String password, String googleId, UserRole role) {
        this.email = email;
        this.password = password;
        this.googleId = googleId;
        this.role = role;
    }

    public UserDTO toUserDTO() {
        return new UserDTO(id, email, googleId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
