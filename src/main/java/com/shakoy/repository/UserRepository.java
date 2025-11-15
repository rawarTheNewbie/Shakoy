package com.shakoy.repository;

import com.shakoy.model.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByUsername(String username);
}
