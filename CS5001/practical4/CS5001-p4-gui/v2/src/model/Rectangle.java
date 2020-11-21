package controller;
import view.Shape;

import java.awt.*;

public class Rectangle extends Shape {

    public Rectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Rectangle() {
        super();
    }

    public void draw(Graphics g) {
        System.out.println("graphics");
        Graphics2D g2 = (Graphics2D) g;
        System.out.println(g);
        System.out.println(g2);
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(4));
        g.drawRect(getX(), getY(), getWidth(), getHeight());
    }

}
