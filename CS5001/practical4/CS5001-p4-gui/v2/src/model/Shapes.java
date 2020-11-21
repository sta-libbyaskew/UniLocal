package model;


import javax.swing.*;
import java.awt.*;

public abstract class Shape extends JPanel {

    public Graphics g;
    public int x1, y1, w, h;
    public Color color;

    public Shape(int x1, int y1, int w, int h, Color color) {
        //this.g  = g;
        this.x1 = x1;
        this.y1 = y1;
        this.w = w;
        this.h = h;
        this.color = color;
    }

    public abstract void draw(Graphics g);

    @Override
    public int getX() {
        return x1;
    }

    @Override
    public int getY() {
        return y1;
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

}
