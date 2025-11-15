package com.shakoy.repository.sqlite;

import com.shakoy.model.Task;
import com.shakoy.model.enums.Priority;
import com.shakoy.model.enums.Status;
import com.shakoy.repository.TaskRepository;
import com.shakoy.util.Db;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTaskRepository implements TaskRepository {
    private final Db db;
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public SqliteTaskRepository(Db db){ this.db = db; }

    private Task map(ResultSet rs) throws SQLException {
        Task t = new Task(rs.getInt("user_id"), rs.getString("title"));
        t.setId(rs.getInt("id"));
        t.setDescription(rs.getString("description"));
        t.setPriority(Priority.values()[Math.max(0, rs.getInt("priority")-1)]);
        t.setStatus(Status.valueOf(rs.getString("status")));
        t.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), F));
        String due = rs.getString("due_at");
        if (due != null) {
            try {
                t.setDueAt(LocalDateTime.parse(due, F));
            } catch (IllegalArgumentException e) {
                // Skip invalid due date (e.g., due date before creation date)
                System.out.println("WARNING: Skipping invalid due date for task " + t.getId() + ": " + e.getMessage());
            }
        }
        String upd = rs.getString("updated_at");
        if (upd != null) t.setUpdatedAt(LocalDateTime.parse(upd, F));
        return t;
    }

    @Override
    public Task save(Task t) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
              "INSERT INTO tasks(user_id,title,description,priority,status,due_at,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?)",
              Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getDescription());
            ps.setInt(4, t.getPriority().code);
            ps.setString(5, t.getStatus().name());
            ps.setString(6, t.getDueAt()==null? null : t.getDueAt().format(F));
            LocalDateTime now = LocalDateTime.now();
            ps.setString(7, now.format(F));
            ps.setString(8, now.format(F));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
            return t;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Task update(Task t) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
              "UPDATE tasks SET title=?, description=?, priority=?, status=?, due_at=?, updated_at=? WHERE id=?")) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setInt(3, t.getPriority().code);
            ps.setString(4, t.getStatus().name());
            ps.setString(5, t.getDueAt()==null? null : t.getDueAt().format(F));
            ps.setString(6, LocalDateTime.now().format(F));
            ps.setInt(7, t.getId());
            ps.executeUpdate();
            return t;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Task> findById(int id) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Task> findAllByUser(int userId) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks WHERE user_id=? ORDER BY COALESCE(due_at, created_at)")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> list = new ArrayList<>();
                System.out.println("DEBUG REPO: Querying tasks for user_id=" + userId);
                while (rs.next()) {
                    System.out.println("DEBUG REPO: Found task id=" + rs.getInt("id") +
                                     ", title=" + rs.getString("title") +
                                     ", priority=" + rs.getInt("priority"));
                    try {
                        Task task = map(rs);
                        list.add(task);
                        System.out.println("DEBUG REPO: Successfully mapped task");
                    } catch (Exception e) {
                        System.out.println("DEBUG REPO: ERROR mapping task: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                System.out.println("DEBUG REPO: Returning " + list.size() + " tasks");
                return list;
            }
        } catch (SQLException e) {
            System.out.println("DEBUG REPO: SQL Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(int id) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tasks WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
