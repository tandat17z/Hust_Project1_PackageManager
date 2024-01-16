package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.Root;
import model.Version;

public class EditConfigFileController{
	Stage stage;
	
	Version verModel;
	String projectName;
	String version;
	
	File configFile;
	String oldContent;
	
	public void setVerModel(Stage stage, Version verModel) {
		this.stage = stage;
		this.verModel = verModel;
		this.projectName = verModel.getProject().getName();
		this.version = verModel.getVersion();
		init();
	}
	
	private void init() {
    	configFile = new File(verModel.getVersionPath(), Root.getInstance(verModel.getProject().getType()).getFileType());
    	StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        oldContent = content.toString();
    	txtCode.setText(oldContent);
    	btnSave.setDisable(true);
    }
	
    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private TextArea txtCode;

    @FXML
    void cancel(ActionEvent event) {
    	this.stage.close();
    }

    @FXML
    void saveConfigFile(ActionEvent event) {
    	Alert waitingAlert = new Alert(AlertType.INFORMATION);
    	waitingAlert.setTitle("Waiting...");
    	waitingAlert.setHeaderText("Vui lòng chờ");
    	waitingAlert.setContentText("Vui lòng chờ...");
    	waitingAlert.show();
    	
    	try {
			verModel.saveChangeConfigFile(getTime(), txtCode.getText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   
        this.stage.close();
        
        waitingAlert.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Cập nhật config file THÀNH CÔNG");
    	alert.setHeaderText(verModel.getProject().getName() + ": " + version);
    	alert.setContentText("HÃY RELOAD LẠI...");
    	alert.show();
        
    }
    
    @FXML
    void checkReleased(KeyEvent event) {
    	if( txtCode.getText().equals(oldContent))
    		btnSave.setDisable(true);
    	else
    		btnSave.setDisable(false);
    }

    private String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date currentDate = new Date();
    	return dateFormat.format(currentDate);
	}
    
}
