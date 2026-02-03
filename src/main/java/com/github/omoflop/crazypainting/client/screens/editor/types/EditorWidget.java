package com.github.omoflop.crazypainting.client.screens.editor.types;

public abstract class EditorWidget {
    public int x;
    public int y;
    public int width;
    public int height;

    public int top() {
        return y;
    }
    public int left() {
        return x;
    }
    public int right() {
        return x + width;
    }
    public int bottom() {
        return y + height;
    }

    public void calculateSize(int screenWidth, int screenHeight) { }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public void alignXCenter(int screenWidth) {
        x = screenWidth / 2 - width / 2;
    }

    public void alignXBetween(int pointA, int pointB) {
        x = (pointB - pointA)/2 - width / 2 + pointA;
    }

    public void alignYCenter(int screenHeight) {
        y = screenHeight / 2 - height / 2;
    }

    public void alignXRight(int distanceFromRight, int screenWidth) {
        x = screenWidth - width - distanceFromRight;
    }

    public void alignXLeft(int distanceFromLeft) {
        x = distanceFromLeft;
    }

    public boolean containsPoint(double px, double py) {
        return x <= px && y <= py && right() > px && bottom() > py;
    }
}
