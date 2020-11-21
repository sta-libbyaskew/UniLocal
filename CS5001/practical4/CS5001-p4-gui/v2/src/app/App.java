package app;

import view.View;
import model.Model;
import controller.Controller;
import model.IModel;
import controller.IController;

public class App {
    public static void main(String[] args) {
        IModel model = new Model();
        IController controller = new Controller(model);
        //new View(model, controller);
        new View();
    }
}