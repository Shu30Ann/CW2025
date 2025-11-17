package com.comp2042;

public enum GameDifficulty {
    EASY(800),      // 800ms per drop - slowest
    MEDIUM(400),    // 400ms per drop - medium speed
    HARD(200);      // 200ms per drop - fastest

    private final int dropDelayMillis;

    GameDifficulty(int dropDelayMillis) {
        this.dropDelayMillis = dropDelayMillis;
    }

    public int getDropDelayMillis() {
        return dropDelayMillis;
    }
}
