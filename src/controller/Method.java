package controller;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Method {
	Stage newStage(String fxml, String title) {
    	Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("/view/" + fxml +".fxml"));
			Scene scene = new Scene(root);
			
			Stage packageStage = new Stage();
			packageStage.setTitle(title);
			packageStage.setScene(scene);
			packageStage.show();
			return packageStage;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
}
