package com.shakoy.service;

import com.shakoy.model.Task;
import com.shakoy.repository.TaskRepository;

import java.util.List;

public class TaskService {
    private final TaskRepository repo;
    public TaskService(TaskRepository repo){ this.repo = repo; }

    public Task create(Task t){ return repo.save(t); }
    public Task update(Task t){ return repo.update(t); }
    public List<Task> allForUser(int userId){ return repo.findAllByUser(userId); }
    public void delete(int id){ repo.deleteById(id); }
}
