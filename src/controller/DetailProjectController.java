package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.Project;
import model.Version;

public class DetailProjectController implements javafx.fxml.Initializable{
	Project curProject;
	
	public void setProjectModel(Project project) {
		curProject = project;
		init();
	}
	
    @FXML
    private AnchorPane apTree;

    @FXML
    private Label lblProjectName;

    @FXML
    private Label lblTime;

    @FXML
    private Label lblType;

    @FXML
    private TableView<Version> tvVersion;

    @FXML
    private TableColumn<Version, String> colDescription;
    
    @FXML
    private TableColumn<Version, String> colTime;
    
    @FXML
    private TableColumn<Version, String> colVersion;
    
    @FXML
    private Button btnNewVersion;
    
    @FXML
    private TextArea txtDescription;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	colVersion.setCellValueFactory(new PropertyValueFactory<Version, String>("version"));
    	colTime.setCellValueFactory(new PropertyValueFactory<Version, String>("time"));
    	colDescription.setCellValueFactory(new PropertyValueFactory<Version, String>("description"));
    	
    }
    
    public void init() {
    	lblProjectName.setText(curProject.getName());
    	lblType.setText(curProject.getType());
    	lblTime.setText(curProject.getTime());
    	txtDescription.setText(curProject.getDescription());
    	
    	showAllVersion();
    }
    
    private void showAllVersion() {
    	// Hiện thị các version trong table view
		tvVersion.setItems( curProject.getVersionOfProject() );
    }
    
    @FXML
    void showVersionDetail(MouseEvent event) {
    	if (event.getClickCount() == 2) {
    		Version version = tvVersion.getSelectionModel().getSelectedItem();
            if (version != null) {
		    	Tab tabVersion = findTagVersion(version);
		    	if( tabVersion == null ) {
		    		tabVersion = new Tab(version.toString());
		    		tabVersion.setClosable(true);
		    		try {
		    			FXMLLoader loader = new FXMLLoader();
		    			loader.setLocation(getClass().getResource("/view/DetailVersion.fxml"));
		    			Node fxmlLoader = loader.load();
		    			DetailVersionController controller = loader.getController();
		    			controller.setVerModel(version);
		    			
		    			tabVersion.setContent(fxmlLoader);
		    			__NodeStatic.tabPane.getTabs().add(tabVersion);
		    		} catch (IOException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
		    	}
		    	__NodeStatic.tabPane.getSelectionModel().select(tabVersion);
            }
    	}
    }
    private Tab findTagVersion(Version version) {
		for (Tab tab : __NodeStatic.tabPane.getTabs()) {
            if (tab.getText().equals(version.toString())) {
                return tab;
            }
        }
        return null; 
	}
    
    // cập nhật mô tả dự án tổng ---------------------------------
    @FXML
    void updateDescription(ActionEvent event) {
    	curProject.setDescription(txtDescription.getText());
    	curProject.save(true);
    	
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Cập nhật mô tả");
    	alert.setHeaderText(curProject.getName());
    	alert.setContentText("Cập nhật thành công description");
    	alert.show();
    }

    @FXML
    void createNewVersion(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/AddVersion.fxml"));
			System.out.println("Add version");
			Node node = loader.load();
			((AddVersionController) loader.getController()).setProject(curProject);
			__NodeStatic.tabProject.setContent(node);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @FXML
    void showHistory() {
    	
    }
}
