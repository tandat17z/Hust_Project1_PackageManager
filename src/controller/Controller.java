package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class Controller implements Initializable {
	@FXML
    private TabPane tabPane;
	
    @FXML
    private Tab tabLibrary;

    @FXML
    private Tab tabProject;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		__NodeStatic.tabProject = tabProject;
		__NodeStatic.tabPane = tabPane;
		
		try {
            // Load FXML file
            FXMLLoader loader0 = new FXMLLoader(getClass().getResource("/view/Project.fxml"));
            Node node = loader0.load();
            __NodeStatic.projectController2 = loader0.getController();
            tabProject.setContent(node);
            
            FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/view/Library.fxml"));
            Node nodeLibrary = loader1.load();
            __NodeStatic.libraryController = loader1.getController();
            tabLibrary.setContent(nodeLibrary);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		// Khi click lại vào tab sẽ reload lại... update dữ liệu mới
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
            	System.out.println("click tab: " + newTab.getText());
            	
                if( newTab.getText().equals("MyProject")) {
                	__NodeStatic.projectController2.reload();
                	if( __NodeStatic.detailProjectController != null ) {
                		__NodeStatic.detailProjectController.init();
                	}
                }
                else if( newTab.getText().equals("Library")) {
            		__NodeStatic.libraryController.reloadAll();
            	}

            }
        });
	}

    	
}
