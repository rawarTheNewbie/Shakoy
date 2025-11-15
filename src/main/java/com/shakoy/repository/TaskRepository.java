package com.shakoy.repository;

import com.shakoy.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Task update(Task task);
    Optional<Task> findById(int id);
    List<Task> findAllByUser(int userId);
    void deleteById(int id);
}
