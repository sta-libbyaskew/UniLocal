package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.BorderLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;


public class View {
    // View uses Swing framework to display UI to user
    private JFrame mainFrame;
    private JMenuBar menu;
    private JButton straightLine;
    private JButton rectangle;
    private JButton ellipse;
    private JButton star;
    private JButton undo;
    private JButton redo;
    private JButton red;
    private JButton green;


    public View() {
        mainFrame = new JFrame("Graphics Drawer");
        menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenuItem load = new JMenuItem("Load");
        file.add(load);
        menu.add(file);
        menu.add(edit);
        load.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(mainFrame, "Not implemented ;-(");
                    }
                });
        mainFrame.setJMenuBar(menu);
        mainFrame.setSize(500, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }
}