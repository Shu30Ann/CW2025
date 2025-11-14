package com.comp2042;

public class PointInt {
    public int x;
    public int y;

    public PointInt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PointInt(PointInt other) {
        this.x = other.x;
        this.y = other.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public String toString() {
        return "PointInt(" + x + ", " + y + ")";
    }
}
