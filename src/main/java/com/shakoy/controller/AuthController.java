package com.shakoy.controller;

import com.shakoy.model.User;
import com.shakoy.service.AuthService;
import com.shakoy.util.DI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Hyperlink registerLink;
    @FXML private Label msg;

    @FXML
    public void onLogin(ActionEvent e) {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        if (u.isEmpty() || p.isEmpty()) {
            msg.setText("Please enter username and password.");
            return;
        }
        AuthService auth = DI.authService;
        var logged = auth.login(u, p);
        if (logged.isPresent()) {
            User user = logged.get();
            loadTasksScene(user.getId()); // Pass user ID
        } else {
            msg.setText("Invalid credentials.");
        }
    }

    @FXML
    public void onRegister(ActionEvent e) {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        if (u.length() < 3 || p.length() < 5) {
            msg.setText("Username ≥3, password ≥5 chars.");
            return;
        }
        try {
            DI.authService.register(u, p);
            msg.setText("Registered! You can log in now.");
        } catch (Exception ex) {
            msg.setText("Registration failed: " + ex.getMessage());
        }
    }

    private void loadTasksScene(int userId) {
        try {
            System.out.println("DEBUG AUTH: Loading tasks scene for userId: " + userId);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/shakoy/view/tasks.fxml"));
            Parent root = loader.load();

            // Get the controller and set the user ID
            TaskController controller = loader.getController();
            System.out.println("DEBUG AUTH: Got controller: " + (controller != null));
            controller.setCurrentUserId(userId);
            System.out.println("DEBUG AUTH: Set user ID");

            Stage stage = (Stage) loginBtn.getScene().getWindow();

            stage.setTitle("Shakoy – Tasks");
            stage.setScene(new Scene(root, 1200, 800));

            // Keep window centered on screen
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
            msg.setText("Error loading tasks screen.");
        }
    }
}