package com.shakoy.util;

import java.sql.*;

public class Db {
    private final String url;

    public Db(String fileName) {
        this.url = "jdbc:sqlite:" + fileName;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void init() {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT UNIQUE NOT NULL," +
                "  password_hash TEXT NOT NULL," +
                "  role TEXT NOT NULL DEFAULT 'USER'," +
                "  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL," +
                "  title TEXT NOT NULL," +
                "  description TEXT," +
                "  priority INTEGER NOT NULL DEFAULT 2," +
                "  status TEXT NOT NULL DEFAULT 'TODO'," +
                "  due_at TEXT," +
                "  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TEXT," +
                "  FOREIGN KEY(user_id) REFERENCES users(id)" +
                ")"
            );

            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_tasks_user_due ON tasks(user_id, due_at)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
