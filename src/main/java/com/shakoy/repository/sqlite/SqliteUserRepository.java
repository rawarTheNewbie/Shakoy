package com.shakoy.repository.sqlite;

import com.shakoy.model.StandardUser;
import com.shakoy.model.User;
import com.shakoy.repository.UserRepository;
import com.shakoy.util.Db;
import java.sql.*;
import java.util.Optional;

public class SqliteUserRepository implements UserRepository {
    private final Db db;
    public SqliteUserRepository(Db db){ this.db = db; }

    @Override
    public User save(User user) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
              "INSERT INTO users(username, password_hash, role) VALUES(?,?,?)",
              Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setId(rs.getInt(1));
            }
            return user;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
              "SELECT id, username, password_hash, role FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StandardUser u = new StandardUser();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setRole(rs.getString("role"));
                    return Optional.of(u);
                }
                return Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
