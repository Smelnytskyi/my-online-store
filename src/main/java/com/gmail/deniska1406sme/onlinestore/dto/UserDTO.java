package com.gmail.deniska1406sme.onlinestore.dto;


import com.gmail.deniska1406sme.onlinestore.validation.OnCreate;
import com.gmail.deniska1406sme.onlinestore.validation.OnUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class UserDTO {
    private Long id;

    @NotNull(message = "Email cannot be empty", groups = OnCreate.class)
    @Email(message = "Please enter a valid email", groups = {OnCreate.class, OnUpdate.class})
    private String email;
    private String googleId;

    public UserDTO() {
    }

    public UserDTO(Long id, String email, String googleId) {
        this.id = id;
        this.email = email;
        this.googleId = googleId;
    }

    public static UserDTO of(Long id, String email, String googleId) {
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", googleId='" + googleId + '\'' +
                '}';
    }
}
