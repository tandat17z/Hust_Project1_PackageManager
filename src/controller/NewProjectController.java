package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import database.MavenProjectRoot;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import model.Project;

public class NewProjectController implements Initializable {
    @FXML
    private Button btnCreate;

    @FXML
    private RadioButton btnGradle, btnMaven, btnNpm;

    @FXML
    private Button btnSelectFile;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtName, txtVersion;

    @FXML
    private Label lblFile;
    
	private File configFile = null;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		btnCreate.setDisable(true);
	}
	
	String getType() {
    	if(btnGradle.isSelected())
    		return "gradle";
    	else if(btnNpm.isSelected())
    		return "npm";
		return "maven";
    }
	String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date currentDate = new Date();
    	return dateFormat.format(currentDate);
	}
	
	void closeStage() {
		MainController.stage_NewProject.close();
	}
	
	void checkCreate() {
		String projectDir = "D:\\Hust_project1_PackageManager\\Project" + "\\" 
				+ txtName.getText() + "\\" + txtVersion;
    	
    	File project = new File(projectDir);
    	if( project.exists() || txtVersion.equals("") || configFile == null) 
    		btnCreate.setDisable(true);
    	else 
    		btnCreate.setDisable(false);
	}
	
	@FXML
    void keyReleased(KeyEvent event) {
		checkCreate();
    }
	
	@FXML
    void mouseReleased(MouseEvent event) {
		checkCreate();
    }
	
    @FXML
    void addConfigFile(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(new File("C:\\Users\\tandat17z\\eclipse-workspace"));
    	configFile = fileChooser.showOpenDialog(null);
    	if( configFile.getName().equals("pom.xml") == false) {
    		configFile = null;
    	}
    	else lblFile.setText(configFile.getPath());	
    }
    
    @FXML
    void createProject(ActionEvent event) {
    	String name = txtName.getText();
    	String version = txtVersion.getText();
    	String type = getType();
    	String time = getTime();
    	String description = txtDescription.getText();
    	
    	Project newProject = new Project(name, version, type, description, time);
    	newProject.saveLocal(configFile);
    	newProject.saveDb();
    	
    	closeStage();
    	
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Success");
    	alert.setHeaderText("Notification");
    	alert.setContentText("Tạo project mới thành công lúc " + getTime());
    	alert.show();
    }

    @FXML
    void cancel(ActionEvent event) {
    	closeStage();
    }
}
