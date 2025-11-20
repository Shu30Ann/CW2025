package com.comp2042;

import java.io.Serializable;

/**
 * Represents a single high score entry.
 * Implements Serializable for file persistence.
 */
public class HighScore implements Serializable, Comparable<HighScore> {

    private static final long serialVersionUID = 1L;
    
    private int score;
    private long timestamp;

    /**
     * Constructor for creating a new high score.
     * @param score The score value
     */
    public HighScore(int score) {
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Compare high scores in descending order (highest first).
     * If scores are equal, compare by timestamp (earlier first).
     */
    @Override
    public int compareTo(HighScore other) {
        if (this.score != other.score) {
            return Integer.compare(other.score, this.score); // Descending order
        }
        return Long.compare(this.timestamp, other.timestamp); // Earlier timestamp comes first
    }

    @Override
    public String toString() {
        return score + " points";
    }
}
