package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import model.MavenProject;

public class Controller implements Initializable {
	static Stage stageNewProject;
    @FXML
    private Button btnSearch;
    
    @FXML
    private ComboBox<String> cbFiller;
    
    @FXML
    private Button btnNewProject;
    
    @FXML
    private Button btnProject;
    
    @FXML
    private TreeView dependencyTree;
    
    @SuppressWarnings("unchecked")
	@Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// TODO Auto-generated method stub
    	// package - filler
    	ObservableList<String> choiceList = javafx.collections.FXCollections.observableArrayList("Maven", "Gradle", "npm");
    	cbFiller.setItems(choiceList);
    	
    	TreeItem<String> rootItem = new TreeItem<>("Files");
    	
    	TreeItem<String> branchItem1 = new TreeItem<>("Files1");
    	TreeItem<String> branchItem2 = new TreeItem<>("Files2");
    	TreeItem<String> branchItem3 = new TreeItem<>("Files3");
    	
    	TreeItem<String> leafItem1 = new TreeItem<>("leaf1");
    	TreeItem<String> leafItem2 = new TreeItem<>("leaf1");
    	TreeItem<String> leafItem3 = new TreeItem<>("leaf1");
    	TreeItem<String> leafItem4 = new TreeItem<>("leaf1");
    	TreeItem<String> leafItem5 = new TreeItem<>("leaf1");
    	
    	branchItem1.getChildren().addAll(leafItem1, leafItem2);
    	branchItem2.getChildren().addAll(leafItem3);
    	branchItem3.getChildren().addAll(leafItem4, leafItem5);
    	
    	rootItem.getChildren().addAll(branchItem1, branchItem2, branchItem3);
    	
    	dependencyTree.setRoot(rootItem);
    }
    public void selectItem() {
    	
    }
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
    @FXML
    void seachAction(ActionEvent event) {
    	Stage stagePackage = newStage("Package", "Thông tin package");
    }
    
    @FXML
    void newProject(ActionEvent event) {
//    	PomReader.readPomFile("C:\\Users\\tandat17z\\eclipse-workspace\\Hust_Project1_PackageManager");
    	stageNewProject = newStage("NewProject", "New project");
    }
    
    @FXML
    void chooseProject(ActionEvent event) {
    	String name = "project1000";
    	String version = "1.0.0";
    	
    	MavenProject prj = new MavenProject(name, version);
    	String groupId = "com.packagemanager";
    	String artifactId = "mavenProject";
    	
    	prj.getDependencyTree();	
    }
    
 
    
    
    
    
}
