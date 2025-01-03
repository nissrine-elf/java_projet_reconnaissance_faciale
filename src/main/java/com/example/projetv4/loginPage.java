package com.example.projetv4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class loginPage extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.createTables();
        FXMLLoader fxmlLoader = new FXMLLoader(loginPage.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Application de reconnaissance faciale");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
