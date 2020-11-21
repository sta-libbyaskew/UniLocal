package controller;
import model.IModel;
import javax.naming.ldap.Control;

public class Controller implements IController{

    private IModel model;

    public Controller(IModel model){
        this.model = model;
    }

    public void controlStraight(){

    }
}


