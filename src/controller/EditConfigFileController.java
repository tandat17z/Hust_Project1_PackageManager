package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import database.ProjectRoot;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.Change;
import model.Library;
import model.Version;

public class EditConfigFileController implements Initializable {
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
	
    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private TextArea txtCode;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// TODO Auto-generated method stub
    	
    }
    
    private void init() {
    	configFile = new File(verModel.getVersionDir(), "pom.xml");
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
    void cancel(ActionEvent event) {
    	this.stage.close();
    }

    @FXML
    void saveConfigFile(ActionEvent event) {
    	System.out.println(verModel.getVersionId());
    	List<Library> oldDependencis = verModel.getDependency();
    	List<Library> newDependencis;

    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(txtCode.getText());
            writer.flush(); // Đảm bảo dữ liệu được ghi vào file ngay lập tức
            writer.close(); // Đóng Writer để giải phóng tài nguyên
            
            String type = verModel.getProject().getType();
            String versionDir = verModel.getVersionDir();
            ProjectRoot.getInstance(type).saveDependencyTree(versionDir);
            
            newDependencis = verModel.getDependency();
            String time = getTime();
            for( Library library: newDependencis) {
            	Boolean check = false;
            	String newName = library.getName(),
        				newVersion = library.getVersion();
            	for(Library old: oldDependencis) {
            		String oldName = old.getName(),
            				oldVersion = old.getVersion();
            		
            		// check update
            		if( newName.equals(oldName)) {
            			check = true;
            			if( !newVersion.equals(oldVersion)){
	            			Change change = new Change(verModel.getVersionId(), time, "update", newName +":" + old.getVersion() + ">>" + library.getVersion());
	            			change.saveDb();
	            		}
            			break;
            		}
            		
            	}
            	
            	// check insert
            	if( !check ) {
            		Change change = new Change(verModel.getVersionId(), time, "insert", newName + ":" + newVersion);
        			change.saveDb();
            	}
            }
            
            // check remove
            for( Library old: oldDependencis) {
            	Boolean check = false;
            	for(Library library: newDependencis) {
            		if(old.getName().equals(library.getName())) {
            			check = true;
            			break;
            		}
            	}
            	if( !check ) {
            		Change change = new Change(verModel.getVersionId(), time, "remove", old.getName() + ":" + old.getVersion());
        			change.saveDb();
            	}
            }
            
            this.stage.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
        	alert.setTitle("Cập nhật config file");
        	alert.setHeaderText(verModel.getProject().getName() + ": " + version);
        	alert.setContentText("Cập nhật file cấu hình thành công ");
        	alert.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void checkReleased(KeyEvent  event) {
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
