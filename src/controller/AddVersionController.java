package controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class AddVersionController {
	private File configFile = null;
	private Project project;
	
	public void setProject(Project project) {
		this.project = project;
		init();
	}
	
    private void init() {
    	btnCreate.setDisable(true);
    	
    	txtName.setText(project.getName());
    	if( project.getType().equals("maven")) {
    		btnMaven.setSelected(true);
    	}
    	else if( project.getType().equals("gradle")) {
    		btnGradle.setSelected(true);
    	}
    	else {
    		btnNpm.setSelected(true);
    	}
    	
	}

	@FXML
    private Button btnCancel, btnCreate;

    @FXML
    private RadioButton btnGradle, btnMaven, btnNpm;

    @FXML
    private Button btnSelectFile;

    @FXML
    private Label lblFile;

    @FXML
    private Label lblVersion;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtVersion;

    @FXML
    private ToggleGroup type;

    @FXML
    void addConfigFile(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(new File("D:\\configFile"));
    	configFile = fileChooser.showOpenDialog(null);

    	checkFile();
    }

    private void checkFile() {
    	if(configFile == null) return;
    	
    	boolean check = true;
		if(project.getType().equals("maven") && !configFile.getName().equals("pom.xml"))
			check = false;
		if(project.getType().equals("gradle") && !configFile.getName().equals("build.gradle"))
			check = false;
		if(project.getType().equals("npm") && !configFile.getName().equals("package.json"))
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
    	
    	String version = txtVersion.getText();
    	if( version.length() == 0) return false;
    	
    	Version newVersion = new Version(project, version);
    	if( !newVersion.exists())  return true;
    	return false;
    }
    
    @FXML
    void addNewVersion(ActionEvent event) {
    	// Lưu version đầu tiên
    	String version = txtVersion.getText();
    	String description = txtDescription.getText();
    	String time = getTime();
    	Version.addNewVerrsion(project, version, description, time, configFile);
    	
    	quayLaiHomepage();
    	
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Thêm version thành công");
    	alert.setHeaderText(project.getName() + ": " + version);
    	alert.setContentText("Thêm version mới thành công lúc " + getTime());
    	alert.show();
    }
    private String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date currentDate = new Date();
    	return dateFormat.format(currentDate);
	}
    @FXML
    void keyReleased(KeyEvent event) {
    	setBtnCreate();
    }

    @FXML
    void cancelStage(ActionEvent event) {
    	quayLaiHomepage();
    }
    
    void quayLaiHomepage() {
    	try {
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(getClass().getResource("/view/Project.fxml"));
    		__NodeStatic.tabProject.setContent(loader.load());
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }

}
