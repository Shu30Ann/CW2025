package com.comp2042;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages high scores for the game.
 * Handles loading, saving, and managing the top 5 high scores.
 */
public class HighScoreManager {

    private static final String HIGH_SCORES_FILE = "highscores.dat";
    private static final int MAX_HIGH_SCORES = 5;
    
    private List<HighScore> highScores;

    /**
     * Constructor that loads existing high scores from file.
     */
    public HighScoreManager() {
        this.highScores = new ArrayList<>();
        loadHighScores();
    }

    /**
     * Load high scores from file. If file doesn't exist, start with empty list.
     */
    private void loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HIGH_SCORES_FILE))) {
            highScores = (List<HighScore>) ois.readObject();
            // Ensure list is sorted in descending order
            Collections.sort(highScores);
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, start with empty list
            highScores = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            // File corrupted or error reading, start fresh
            System.err.println("Error loading high scores: " + e.getMessage());
            highScores = new ArrayList<>();
        }
    }

    /**
     * Save high scores to file.
     */
    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORES_FILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    /**
     * Add a new score to the high scores list.
     * If the list is not full, simply add the score.
     * If the list is full, only add if it's higher than the lowest score.
     * 
     * @param score The score to add
     * @return true if the score was added, false otherwise
     */
    public boolean addScore(int score) {
        HighScore newScore = new HighScore(score);
        
        // If list is not full, add the score
        if (highScores.size() < MAX_HIGH_SCORES) {
            highScores.add(newScore);
            Collections.sort(highScores);
            saveHighScores();
            return true;
        }
        
        // If list is full, check if new score is higher than the lowest
        HighScore lowestScore = highScores.get(highScores.size() - 1);
        if (score > lowestScore.getScore()) {
            highScores.remove(highScores.size() - 1); // Remove lowest
            highScores.add(newScore);
            Collections.sort(highScores);
            saveHighScores();
            return true;
        }
        
        return false;
    }

    /**
     * Get all high scores sorted in descending order.
     * 
     * @return List of high scores
     */
    public List<HighScore> getHighScores() {
        return new ArrayList<>(highScores);
    }

    /**
     * Get the top N high scores.
     * 
     * @param count Number of scores to return
     * @return List of top N scores
     */
    public List<HighScore> getTopScores(int count) {
        List<HighScore> topScores = new ArrayList<>(highScores);
        if (topScores.size() > count) {
            return topScores.subList(0, count);
        }
        return topScores;
    }

    /**
     * Check if a score qualifies for the high score list.
     * 
     * @param score The score to check
     * @return true if the score qualifies, false otherwise
     */
    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_HIGH_SCORES) {
            return true;
        }
        HighScore lowestScore = highScores.get(highScores.size() - 1);
        return score > lowestScore.getScore();
    }

    /**
     * Clear all high scores (for testing purposes).
     */
    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }

    /**
     * Get the number of high scores stored.
     * 
     * @return Number of scores
     */
    public int getScoreCount() {
        return highScores.size();
    }
}
