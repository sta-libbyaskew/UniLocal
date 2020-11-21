package model;

import java.awt.*;

public class Polygon extends Shapes {
        public Polygon(int x1, int y1, int w, int h, Color color, int sides, boolean filled, int thickness)  {
            super(x1 , y1 , w , h , color, sides ,filled, thickness);
        }

        public void draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            if (filled){
                g.fillPolygon(xpolyPoints(sides) , ypolyPoints(sides) , sides);
            }
            else{
                g.drawPolygon(xpolyPoints(sides) , ypolyPoints(sides) , sides);
            }

        }
    }

