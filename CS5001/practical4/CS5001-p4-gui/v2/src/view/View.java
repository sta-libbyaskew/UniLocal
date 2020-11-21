package view;
import controller.IController;
import model.IModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.*;


public class View extends Observable implements ActionListener {
    // View uses Swing framework to display UI to user
    int startX, startY, endX, endY, w, h;
    ArrayList<Shape> shapeList = new ArrayList<Shape>();
    private IModel model;
    private IController controller;
    private JFrame mainFrame;
    private JPanel drawPanel;
    private JMenuBar menu;
    private JButton straightline;
    private JButton rectangle;
    private JButton ellipse;
    private JButton star;
    private JButton undo;
    private JButton redo;
    private JButton red;
    private JButton green;
    private Container cp;


    //public View(IModel model , IController controller) {
    public View() {
        mainFrame = new JFrame("Graphics Drawer"); //TODO: break these up into induvidual methods.
        menu = new JMenuBar();
        drawPanel = new JPanel();
        cp = mainFrame.getContentPane();
        JMenu edit = new JMenu("Edit:");
        JMenu shapes = new JMenu("Shapes:");
        JMenu colour = new JMenu("Colour:");
        JMenuItem undo = new JMenuItem("undo");
        JMenuItem redo = new JMenuItem("redo");
        JMenuItem straightline = new JMenuItem("Straight Line");
        JMenuItem rectangle = new JMenuItem("Rectangle");
        JMenuItem ellipse = new JMenuItem("Ellipse");
        JMenuItem star = new JMenuItem("Star");
        JMenuItem red = new JMenuItem("Red");
        JMenuItem green = new JMenuItem("Green");
        JMenuItem blue = new JMenuItem("Blue");
        menu.add(edit);
        menu.add(shapes);
        menu.add(colour);
        edit.add(undo);
        edit.add(redo);
        shapes.add(straightline);
        shapes.add(rectangle);
        shapes.add(ellipse);
        shapes.add(star);
        shapes.add(star);
        colour.add(red);
        colour.add(green);
        colour.add(blue);
        rectangle.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("straight line");
                        //controller.controlStraight();
                        cp.addMouseListener(new MouseAdapter() {
                            public void mousePressed(MouseEvent e) {
                                startX = e.getX();
                                startY = e.getY();
                                System.out.println("mouse press");
                            }

                            public void mouseReleased(MouseEvent e) {
                                endX = e.getX();
                                endY = e.getY();
                                System.out.println("mouse release");
                                int width = startX - endX;
                                int height = startY - endY;
                                w = Math.abs(width);
                                h = Math.abs(height);
                                Rectangle r = new Rectangle(startX, startY, h, w);
                                Graphics g1 = mainFrame.getGraphics();
                                Graphics2D g2 = (Graphics2D) g1;
                                System.out.println(g1);
                                System.out.println(g2);
                                g2.setColor(Color.RED);
                                g2.setStroke(new BasicStroke(4));
                                g1.drawRect(startX,startY, w, h);
                                System.out.println("drawn?");
                                shapeList.add(r);
                                //mainFrame.repaint();
                                /**
                                 mainFrame.repaint();
                                 shapeList.add(r);
                                 System.out.println("pre draw");
                                 System.out.println(mainFrame.getGraphics());
                                 g.setColor(Color.black);
                                 g.drawRect(startX,startY, w, h);
                                 System.out.println("past draw");
                                 mainFrame.repaint();
                                 */
                            }
                        });
                    }
                });
        mainFrame.setJMenuBar(menu);
        mainFrame.setSize(500, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

    public void paint(Graphics g) {

        for (Shape s : shapeList) {
            s.draw(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
    /**
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == straightline) {
            System.out.println("straight line");
            controller.controlStraight();
            cp.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e) {
                    startX = e.getX();
                    startY = e.getY();
                    System.out.println("mouse press");
                }
                public void mouseReleased(MouseEvent e) {
                    endX = e.getX();
                    endY = e.getY();
                    System.out.println("mouse release??");
                    int width = startX - endX;
                    int height = startY - endY;
                    System.out.println("?");
                    w = Math.abs(width);
                    h = Math.abs(height);
                    g = (Graphics2D) g;
                    Rectangle r =  new Rectangle(startX, startY, w, h);
                    shapeList.add(r);
                    System.out.println("pre draw");
                    r.draw(g);
                    System.out.println("past draw");
                    mainFrame.repaint();
                }
        });
    }*/

//TODO: add action listener for each
/**
 load.addActionListener(
 new ActionListener() {
 public void actionPerformed(ActionEvent e) {
 JOptionPane.showMessageDialog(mainFrame, "Not implemented ;-(");
 }
 });
 */