package com.shakoy.service;

import com.shakoy.model.StandardUser;
import com.shakoy.model.User;
import com.shakoy.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {
    private final UserRepository repo;
    private User currentUser;

    public AuthService(UserRepository repo) { this.repo = repo; }

    public Optional<User> login(String username, String password) {
        Optional<User> u = repo.findByUsername(username);
        if (u.isPresent() && BCrypt.checkpw(password, u.get().getPasswordHash())) {
            currentUser = u.get();
            return u;
        }
        return Optional.empty();
    }

    public User register(String username, String password) {
        StandardUser u = new StandardUser();
        u.setUsername(username);
        u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        u.setRole("USER");
        return repo.save(u);
    }

    public void ensureDemoUser() {
        if (repo.findByUsername("demo").isEmpty()) {
            register("demo", "demo123");
        }
    }

    public User currentUser() { return currentUser; }
}
