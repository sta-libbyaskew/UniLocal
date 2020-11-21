package model;

import java.awt.*;

public class Rectangle extends Shapes {

    public Rectangle(int x1,int y1,int w,int h,Color color)  {
        super(x1 , y1 , w , h , color);
    }

    public void draw(Graphics g) { //should graphics be passed here or in  the constructor?
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));
        g.drawRect(x1, y1, w, h);
    }
}
