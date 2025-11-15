package com.shakoy.util;

import com.shakoy.repository.TaskRepository;
import com.shakoy.repository.UserRepository;
import com.shakoy.repository.sqlite.SqliteTaskRepository;
import com.shakoy.repository.sqlite.SqliteUserRepository;
import com.shakoy.service.AuthService;
import com.shakoy.service.TaskService;

public class DI {
    public static Db db;
    public static UserRepository userRepository;
    public static TaskRepository taskRepository;
    public static AuthService authService;
    public static TaskService taskService;

    public static void bootstrap() {
        db = new Db("shakoy.db");
        db.init();
        userRepository = new SqliteUserRepository(db);
        taskRepository = new SqliteTaskRepository(db);
        authService = new AuthService(userRepository);
        taskService = new TaskService(taskRepository);
    authService.ensureDemoUser();
    }
}
