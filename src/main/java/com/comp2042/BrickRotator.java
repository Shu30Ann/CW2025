package com.comp2042;

import com.comp2042.logic.bricks.Brick;

public class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    public int getCurrentOrientation() {
        return currentShape;
    }

    public NextShapeInfo getNextShape() {
        int nextShape = currentShape;
        nextShape = (++nextShape) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    public int[][] getShapeAt(int orientation) {
        int normalized = normalizeOrientation(orientation);
        return brick.getShapeMatrix().get(normalized);
    }

    public int getOrientationCount() {
        return brick.getShapeMatrix().size();
    }

    public void setCurrentShape(int currentShape) {
        this.currentShape = normalizeOrientation(currentShape);
    }

    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }

    public Brick getBrick() {
        return brick;
    }

    private int normalizeOrientation(int orientation) {
        int size = brick.getShapeMatrix().size();
        int mod = orientation % size;
        return mod < 0 ? mod + size : mod;
    }

}
