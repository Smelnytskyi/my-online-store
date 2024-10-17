package com.gmail.deniska1406sme.onlinestore.repositories;

import com.gmail.deniska1406sme.onlinestore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    User findByEmail(String email);

    User findByGoogleId(String googleId);
}
