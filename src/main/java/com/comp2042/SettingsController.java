package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import java.io.IOException;

public class SettingsController {

    @FXML private Slider musicSlider;
    @FXML private Slider sfxSlider;
    @FXML private Button saveButton;

    private Scene returnScene;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        musicSlider.setValue(SoundManager.getMasterVolume());
        sfxSlider.setValue(SoundManager.getSfxVolume());

        // Preview volume changes while sliding.
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                SoundManager.setMasterVolume(newVal.doubleValue()));

        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                SoundManager.setSfxVolume(newVal.doubleValue()));
    }

    @FXML
    private void saveSettings() {
        SoundManager.setMasterVolume(musicSlider.getValue());
        SoundManager.setSfxVolume(sfxSlider.getValue());
        navigateBack();
    }

    private void navigateBack() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();

            if (returnScene != null) {
                stage.setScene(returnScene);
                stage.show();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainMenu.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root, 800, 600));
                stage.show();
            }

            if (onSaved != null) {
                onSaved.run();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to open main menu");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void setReturnScene(Scene scene) {
        this.returnScene = scene;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }
}
