package com.shakoy.model;

import com.shakoy.model.enums.Priority;
import com.shakoy.model.enums.Status;
import java.time.LocalDateTime;

public class Task {
    private Integer id;
    private int userId;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime dueAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task(int userId, String title) {
        this.userId = userId;
        this.title = title;
        this.priority = Priority.MEDIUM;
        this.status = Status.TODO;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) {
        if (dueAt != null && dueAt.isBefore(createdAt)) throw new IllegalArgumentException("Due cannot be before creation");
        this.dueAt = dueAt; this.updatedAt = LocalDateTime.now();
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
