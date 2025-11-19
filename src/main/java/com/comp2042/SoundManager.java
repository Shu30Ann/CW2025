package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {
    
    private static MediaPlayer backgroundMusicPlayer;
    
    /**
     * Play background music on loop
     */
    public static void playBackgroundMusic() {
        try {
            URL resource = SoundManager.class.getResource("/backgroundMusic.mp3");
            if (resource != null) {
                Media media = new Media(resource.toString());
                backgroundMusicPlayer = new MediaPlayer(media);
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusicPlayer.setVolume(0.5);
                backgroundMusicPlayer.play();
                System.out.println("✓ Background music started");
            } else {
                System.out.println("❌ backgroundMusic.mp3 not found!");
            }
        } catch (Exception e) {
            System.err.println("Failed to play background music: " + e.getMessage());
        }
    }
    
    /**
     * Stop the background music
     */
    public static void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }
    
    /**
     * Play line clear sound effect
     */
    public static void playLineClearSound() {
        playSoundEffect("/lineClearMusic.mp3", 0.8);
    }
    
    /**
     * Play game over sound effect
     */
    public static void playGameOverSound() {
        playSoundEffect("/gameOverMusic.mp3", 0.9);
    }
    
    /**
     * Generic method to play a sound effect with specified volume
     */
    private static void playSoundEffect(String resourcePath, double volume) {
        try {
            URL resource = SoundManager.class.getResource(resourcePath);
            if (resource != null) {
                Media media = new Media(resource.toString());
                
                // Create a new player for each sound effect
                // This allows the sound to play even if it's triggered rapidly
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(volume);
                player.play();
                
                // Stop and release resources when finished
                player.setOnEndOfMedia(() -> player.dispose());
            } else {
                System.err.println("Sound file not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to play sound: " + e.getMessage());
        }
    }
}
