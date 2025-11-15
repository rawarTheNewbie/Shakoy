package com.shakoy.app;

import com.shakoy.util.DI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DI.bootstrap();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/shakoy/view/login.fxml"));
        Parent root = loader.load();
        stage.setTitle("shakoy — edited");
        System.out.println("[shakoy] starting (edited main) — DI and UI initialized");
        stage.setScene(new Scene(root, 420, 520));
        stage.centerOnScreen(); // Center the window on initial launch
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
