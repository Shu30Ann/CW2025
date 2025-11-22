package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {

    private static final String GAME_MUSIC_PATH = "/inGameMusic.mp3";

    private static MediaPlayer backgroundMusicPlayer;
    private static double masterVolume = 0.5;
    private static double sfxVolume = 0.8;
    private static boolean sfxEnabled = true;

    /**
     * Play background music on loop.
     */
    public static void playBackgroundMusic() {
        try {
            if (backgroundMusicPlayer == null) {
                URL resource = SoundManager.class.getResource(GAME_MUSIC_PATH);
                if (resource == null) {
                    System.out.println("inGameMusic.mp3 not found!");
                    return;
                }

                Media media = new Media(resource.toString());
                backgroundMusicPlayer = new MediaPlayer(media);
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }

            backgroundMusicPlayer.setVolume(masterVolume);
            backgroundMusicPlayer.play();
            System.out.println("Background music started");
        } catch (Exception e) {
            System.err.println("Failed to play background music: " + e.getMessage());
        }
    }

    /**
     * Stop the background music.
     */
    public static void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }

    /**
     * Play line clear sound effect.
     */
    public static void playLineClearSound() {
        playSoundEffect("/lineClearMusic.mp3", 0.8);
    }

    /**
     * Play game over sound effect.
     */
    public static void playGameOverSound() {
        playSoundEffect("/gameOverMusic.mp3", 0.9);
    }

    /**
     * Generic method to play a sound effect with specified volume.
     */
    private static void playSoundEffect(String resourcePath, double volume) {
        if (!sfxEnabled || sfxVolume <= 0) {
            return;
        }

        try {
            URL resource = SoundManager.class.getResource(resourcePath);
            if (resource != null) {
                Media media = new Media(resource.toString());

                // Create a new player for each sound effect so rapid triggers do not cut each other off.
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(clamp(volume * sfxVolume));
                player.play();

                // Stop and release resources when finished.
                player.setOnEndOfMedia(player::dispose);
            } else {
                System.err.println("Sound file not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to play sound: " + e.getMessage());
        }
    }

    public static double getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(double volume) {
        masterVolume = clamp(volume);
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(masterVolume);
        }
    }

    public static double getSfxVolume() {
        return sfxVolume;
    }

    public static void setSfxVolume(double volume) {
        sfxVolume = clamp(volume);
    }

    public static boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public static void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }

    private static double clamp(double value) {
        if (value < 0) return 0;
        if (value > 1) return 1;
        return value;
    }
}
