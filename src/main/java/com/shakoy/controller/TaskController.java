package com.shakoy.controller;

import com.shakoy.model.Task;
import com.shakoy.model.enums.Priority;
import com.shakoy.model.enums.Status;
import com.shakoy.service.TaskService;
import com.shakoy.util.DI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TaskController {
    
    @FXML private VBox tasksContainer;
    @FXML private Label todoCountLabel;
    @FXML private Label progressCountLabel;
    @FXML private Label completedCountLabel;
    
    @FXML private Button filterAllBtn;
    @FXML private Button filterTodoBtn;
    @FXML private Button filterProgressBtn;
    @FXML private Button filterCompletedBtn;
    @FXML private Button filterAllPriorityBtn;
    @FXML private Button filterLowBtn;
    @FXML private Button filterMediumBtn;
    @FXML private Button filterHighBtn;
    
    private TaskService taskService;
    private Status currentStatusFilter = null;
    private Priority currentPriorityFilter = null;
    private int currentUserId = 1;
    
    public void initialize() {
        System.out.println("DEBUG: TaskController initialized");
        taskService = DI.taskService;
    }

    
    public void setCurrentUserId(int userId) {
        System.out.println("DEBUG: setCurrentUserId called with userId: " + userId);
        this.currentUserId = userId;
        loadTasks();
        updateStats();
    }
    
    private void loadTasks() {
        tasksContainer.getChildren().clear();
        List<Task> tasks = taskService.allForUser(currentUserId);
        
        System.out.println("DEBUG: Loading tasks for user " + currentUserId);
        System.out.println("DEBUG: Found " + tasks.size() + " total tasks");
        
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(15);
        flowPane.setVgap(15);
        flowPane.setPrefWrapLength(1100);
        
        int shownTasks = 0;
        for (Task task : tasks) {
            System.out.println("DEBUG: Task - " + task.getTitle() + " | Status: " + task.getStatus() + " | Priority: " + task.getPriority());
            if (shouldShowTask(task)) {
                flowPane.getChildren().add(createTaskCard(task));
                shownTasks++;
            } else {
                System.out.println("DEBUG: Task filtered out");
            }
        }
        
        System.out.println("DEBUG: Showing " + shownTasks + " tasks after filtering");
        
        if (flowPane.getChildren().isEmpty()) {
            showEmptyState();
        } else {
            tasksContainer.getChildren().add(flowPane);
        }
    }
    
    private boolean shouldShowTask(Task task) {
        boolean statusMatch = currentStatusFilter == null || task.getStatus() == currentStatusFilter;
        boolean priorityMatch = currentPriorityFilter == null || task.getPriority() == currentPriorityFilter;
        return statusMatch && priorityMatch;
    }
    
    private VBox createTaskCard(Task task) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; " +
                     "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(350);
        card.setMaxWidth(350);
        card.setMinHeight(200);
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        HBox.setHgrow(titleBox, javafx.scene.layout.Priority.ALWAYS);
        
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(260);
        
        Label descLabel = new Label(task.getDescription() != null ? task.getDescription() : "");
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-cursor: hand;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(260);
        descLabel.setMaxHeight(60); // Limit height to ~3 lines
        descLabel.setMinHeight(40); // Ensure minimum space
        descLabel.setEllipsisString("..."); // Show ellipsis for overflow

        // Add tooltip to show full description on hover
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Tooltip tooltip = new Tooltip(task.getDescription());
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            Tooltip.install(descLabel, tooltip);

            // Make description clickable to show full text in a dialog
            descLabel.setOnMouseClicked(e -> {
                e.consume(); // Stop event propagation
                showDescriptionDialog(task.getTitle(), task.getDescription());
            });
        }
        
        titleBox.getChildren().addAll(titleLabel, descLabel);

        // Color-coded status indicator circle
        Button statusBtn = new Button();
        statusBtn.setPrefSize(30, 30);
        statusBtn.setMinSize(30, 30);
        statusBtn.setMaxSize(30, 30);

        String statusColor;
        switch (task.getStatus()) {
            case DONE:
                statusColor = "#10b981"; // Green
                break;
            case IN_PROGRESS:
                statusColor = "#f59e0b"; // Yellow/Orange
                break;
            case TODO:
            default:
                statusColor = "#3b82f6"; // Blue
                break;
        }

        statusBtn.setStyle("-fx-background-color: " + statusColor + "; " +
                          "-fx-background-radius: 50%; " +
                          "-fx-cursor: hand; " +
                          "-fx-border-color: transparent;");
        statusBtn.setOnAction(e -> toggleTaskStatus(task));

        header.getChildren().addAll(titleBox, statusBtn);
        
        HBox badges = new HBox(8);
        
        Label statusBadge = new Label(task.getStatus().name().replace("_", " "));
        statusBadge.setStyle(getStatusStyle(task.getStatus()));
        statusBadge.setPadding(new Insets(5, 12, 5, 12));
        
        Label priorityBadge = new Label(task.getPriority().name());
        priorityBadge.setStyle(getPriorityStyle(task.getPriority()));
        priorityBadge.setPadding(new Insets(5, 12, 5, 12));
        
        badges.getChildren().addAll(statusBadge, priorityBadge);
        
        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dateText = task.getDueAt() != null ? task.getDueAt().format(formatter) : "No due date";
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        dateBox.getChildren().addAll(dateIcon, dateLabel);
        
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");
        
        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        editBtn.setPrefWidth(150);
        editBtn.setOnAction(e -> editTask(task));
        
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; " +
                          "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteBtn.setPrefWidth(150);
        deleteBtn.setOnAction(e -> deleteTask(task));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(header, badges, dateBox, actions);
        
        return card;
    }
    
    private String getStatusStyle(Status status) {
        switch (status) {
            case TODO:
                return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                       "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; " +
                       "-fx-border-color: #93c5fd; -fx-border-width: 1; -fx-border-radius: 6;";
            case IN_PROGRESS:
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                       "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; " +
                       "-fx-border-color: #fcd34d; -fx-border-width: 1; -fx-border-radius: 6;";
            case DONE:
                return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; " +
                       "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; " +
                       "-fx-border-color: #6ee7b7; -fx-border-width: 1; -fx-border-radius: 6;";
            default:
                return "";
        }
    }
    
    private String getPriorityStyle(Priority priority) {
        switch (priority) {
            case LOW:
                return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                       "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;";
            case MEDIUM:
                return "-fx-background-color: #ffedd5; -fx-text-fill: #c2410c; " +
                       "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;";
            case HIGH:
                return "-fx-background-color: #ffe4e6; -fx-text-fill: #be123c; " +
                       "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "";
        }
    }
    
    private void toggleTaskStatus(Task task) {
        Status[] statuses = Status.values();
        int currentIndex = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] == task.getStatus()) {
                currentIndex = i;
                break;
            }
        }
        Status nextStatus = statuses[(currentIndex + 1) % statuses.length];
        task.setStatus(nextStatus);
        task.setUpdatedAt(LocalDateTime.now());
        taskService.update(task);
        loadTasks();
        updateStats();
    }
    
    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-padding: 60; -fx-background-color: white; " +
                           "-fx-background-radius: 12; -fx-border-color: #e2e8f0; " +
                           "-fx-border-width: 1; -fx-border-radius: 12;");
        
        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 48px;");
        
        Label title = new Label("No tasks found");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        
        Label subtitle = new Label("Create a new task to get started");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        
        emptyState.getChildren().addAll(icon, title, subtitle);
        tasksContainer.getChildren().add(emptyState);
    }
    
    @FXML
    private void showAddTaskDialog() {
        showTaskDialog(null);
    }
    
    private void editTask(Task task) {
        showTaskDialog(task);
    }
    
    private void showTaskDialog(Task existingTask) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(existingTask == null ? "New Task" : "Edit Task");
        dialog.setHeaderText(existingTask == null ? "Create a new task" : "Edit task details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        if (existingTask != null) titleField.setText(existingTask.getTitle());
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        if (existingTask != null && existingTask.getDescription() != null) {
            descArea.setText(existingTask.getDescription());
        }
        
        ComboBox<Status> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(Status.values());
        statusBox.setValue(existingTask != null ? existingTask.getStatus() : Status.TODO);
        
        ComboBox<Priority> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(existingTask != null ? existingTask.getPriority() : Priority.MEDIUM);
        
        DatePicker datePicker = new DatePicker();
        if (existingTask != null && existingTask.getDueAt() != null) {
            datePicker.setValue(existingTask.getDueAt().toLocalDate());
        }
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusBox, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityBox, 1, 3);
        grid.add(new Label("Due Date:"), 0, 4);
        grid.add(datePicker, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    return null;
                }
                
                Task task;
                if (existingTask != null) {
                    task = existingTask;
                    task.setTitle(titleField.getText());
                    task.setDescription(descArea.getText());
                    task.setStatus(statusBox.getValue());
                    task.setPriority(priorityBox.getValue());
                } else {
                    task = new Task(currentUserId, titleField.getText());
                    task.setDescription(descArea.getText());
                    task.setStatus(statusBox.getValue());
                    task.setPriority(priorityBox.getValue());
                }
                
                if (datePicker.getValue() != null) {
                    task.setDueAt(datePicker.getValue().atStartOfDay());
                }
                
                return task;
            }
            return null;
        });
        
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            if (existingTask == null) {
                taskService.create(task);
            } else {
                taskService.update(task);
            }
            loadTasks();
            updateStats();
        });
    }
    
    private void deleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText(task.getTitle());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskService.delete(task.getId());
            loadTasks();
            updateStats();
        }
    }

    private void showDescriptionDialog(String title, String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Description");
        alert.setHeaderText(title);

        TextArea textArea = new TextArea(description);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
    
    private void updateStats() {
        List<Task> allTasks = taskService.allForUser(currentUserId);
        long todoCount = allTasks.stream().filter(t -> t.getStatus() == Status.TODO).count();
        long progressCount = allTasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
        long completedCount = allTasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        
        todoCountLabel.setText(String.valueOf(todoCount));
        progressCountLabel.setText(String.valueOf(progressCount));
        completedCountLabel.setText(String.valueOf(completedCount));
    }
    
    private void resetFilterButtons() {
        String inactiveStyle = "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-cursor: hand;";
        filterAllBtn.setStyle(inactiveStyle);
        filterTodoBtn.setStyle(inactiveStyle);
        filterProgressBtn.setStyle(inactiveStyle);
        filterCompletedBtn.setStyle(inactiveStyle);
        filterAllPriorityBtn.setStyle(inactiveStyle);
        filterLowBtn.setStyle(inactiveStyle);
        filterMediumBtn.setStyle(inactiveStyle);
        filterHighBtn.setStyle(inactiveStyle);
    }
    
    @FXML
    private void filterAll() {
        resetFilterButtons();
        currentStatusFilter = null;
        filterAllBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterTodo() {
        resetFilterButtons();
        currentStatusFilter = Status.TODO;
        filterTodoBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterInProgress() {
        resetFilterButtons();
        currentStatusFilter = Status.IN_PROGRESS;
        filterProgressBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterCompleted() {
        resetFilterButtons();
        currentStatusFilter = Status.DONE;
        filterCompletedBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterAllPriority() {
        resetFilterButtons();
        currentPriorityFilter = null;
        filterAllPriorityBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterLowPriority() {
        resetFilterButtons();
        currentPriorityFilter = Priority.LOW;
        filterLowBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterMediumPriority() {
        resetFilterButtons();
        currentPriorityFilter = Priority.MEDIUM;
        filterMediumBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void filterHighPriority() {
        resetFilterButtons();
        currentPriorityFilter = Priority.HIGH;
        filterHighBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loadTasks();
    }
    
    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/shakoy/view/login.fxml"));
            Stage stage = (Stage) tasksContainer.getScene().getWindow();

            stage.setScene(new Scene(root, 420, 520));

            // Keep window centered on screen
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}