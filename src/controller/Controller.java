package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;

public class Controller implements Initializable {

    @FXML
    private Tab tabLibrary;

    @FXML
    private Tab tabProject;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		try {
            // Load FXML file
            FXMLLoader loader0 = new FXMLLoader(getClass().getResource("/view/Project.fxml"));
            tabProject.setContent(loader0.load());
            FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/view/Library.fxml"));
            tabLibrary.setContent(loader1.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    	
}
