package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import model.Project;
import model.Version;

public class CreateProjectController implements Initializable {
	private File configFile = null;
	
    @FXML
    private Button btnCancel, btnCreate;

    @FXML
    private RadioButton btnGradle, btnMaven, btnNpm;

    @FXML
    private Button btnSelectFile;

    @FXML
    private Label lblFile;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtName, txtVersion;

    @FXML
    private ToggleGroup type;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// TODO Auto-generated method stub
    	btnCreate.setDisable(true);
    }
    
    @FXML
    void createProject(ActionEvent event) {
    	String name = txtName.getText();
    	String type = getType();
    	String time = getTime();
    	String description = txtDescription.getText();
    	String version = txtVersion.getText();
    	
    	Project.createNewProject(name, type, time, description, version, configFile);
    	
    	quayLaiHomepage();
    	
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle(type);
    	alert.setHeaderText(name + ": " + version);
    	alert.setContentText("Tạo project mới thành công lúc " + getTime());
    	alert.show();
    }

	@FXML
    void addConfigFile(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(new File("D:\\"));
    	
    	configFile = fileChooser.showOpenDialog(null);
    	checkFile();
    }

	@FXML
    void checkChoiceType(ActionEvent event) {
    	checkFile();
    }
	
	@FXML
    void keyReleased(KeyEvent event) {
		setBtnCreate();
    }
	
	@FXML
    void cancelStage(ActionEvent event) {
    	quayLaiHomepage();
    }
	
	private void quayLaiHomepage() {
    	try {
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(getClass().getResource("/view/Project.fxml"));
			__NodeStatic.tabProject.setContent(loader.load());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    private void checkFile() {
    	if(configFile == null) return;
    	
    	boolean check = true;
		if(getType().equals("maven") && !configFile.getName().equals("pom.xml"))
			check = false;
		if(getType().equals("gradle") && !configFile.getName().equals("build.gradle"))
			check = false;
		if(getType().equals("npm") && !configFile.getName().equals("package.json"))
			check = false;
		
		if( check == false) {
    		configFile = null;
    		lblFile.setText("File không hợp lệ. Hãy chọn lại !!!");
    	}
    	else {
    		lblFile.setText(configFile.getPath());	
    	}
		setBtnCreate();
	}
    
	private void setBtnCreate() {
		if(checkCreate()) {
			btnCreate.setDisable(false);
		}
		else
			btnCreate.setDisable(true);
	}
	
    private boolean checkCreate() {
    	if(configFile == null) return false;
    	
    	String name = txtName.getText();
    	String version = txtVersion.getText();
    	if( name.length() == 0 || version.length() == 0) return false;
    	
    	Project newProject = new Project(name);
    	Version newVersion = new Version(newProject, version);
    	if( !newProject.exists() && !newVersion.exists())  return true;
    	return false;
    }
	
    private String getType() {
    	if(btnGradle.isSelected())
    		return "gradle";
    	else if(btnNpm.isSelected())
    		return "npm";
		return "maven";
    }
    
    private String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date currentDate = new Date();
    	return dateFormat.format(currentDate);
	}

    
}
