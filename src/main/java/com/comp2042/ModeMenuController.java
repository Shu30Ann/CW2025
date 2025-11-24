package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class ModeMenuController {

    @FXML
    private VBox menuRoot;

    @FXML
    private void startBasic() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/difficultyMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) menuRoot.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startVersus() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/versusLayout.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) menuRoot.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) menuRoot.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
