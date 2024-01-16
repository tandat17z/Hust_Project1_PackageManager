package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import database.Database;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import model.Project;
import model.Version;
 
public class ProjectController implements Initializable{
	@FXML
    private Button btnCreateProject;
	
	@FXML
    private Button btnOpenProject;
	
    @FXML
    private Button btnReload;

    @FXML
    private Button btnSearch;

    @FXML
    private TableView<Project> tvProject;
    
    @FXML
    private TableColumn<Project, String> colName;

    @FXML
    private TableColumn<Project, String> colType;

    @FXML
    private TableColumn<Project, String> colTime;
    
    @FXML
    private AnchorPane paneShowProject;

    @FXML
    private TextField txtSearch;

    @FXML
    private VBox vboxRecent;


	FilteredList<Project> filteredProject;
	
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// TODO Auto-generated method stub
    	colType.setCellValueFactory(new PropertyValueFactory<Project, String>("type"));
    	colName.setCellValueFactory(new PropertyValueFactory<Project, String>("name"));
    	colTime.setCellValueFactory(new PropertyValueFactory<Project, String>("time"));
    	reload();
    }
    
    public void reload() {
    	reloadProject();
    	reloadRecent();
	}

	@FXML
    void createProject(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/CreateProject.fxml"));
			System.out.println("new project btn");
			__NodeStatic.tabProject.setContent(loader.load());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @FXML
    void openProject(ActionEvent event) {
    	DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Chọn 1 project đã lưu để mở");
        
        directoryChooser.setInitialDirectory(new File("D:\\Hust_project1_PackageManager\\Project"));
        File directory = directoryChooser.showDialog(null);
        
        if (directory != null) {
        	showProject(new Project(directory.getName()));
        } else {
            System.out.println("Không có thư mục nào được chọn.");
        }
    }

    void reloadProject() {
    	// Load Project --------------------------------
		filteredProject = new FilteredList<>(Project.getAll());
		tvProject.setItems(filteredProject);
    }
    
    @FXML
    void searchProject(ActionEvent event) {
    	if( this.paneShowProject.getChildren().size() > 2)
    		this.paneShowProject.getChildren().removeLast();
    	
    	reloadProject();
    	String search = txtSearch.getText();
    	filteredProject.setPredicate(project -> project.getName().contains(search));
    }

    @FXML
    void showProjectDetail(MouseEvent event) {
    	if (event.getClickCount() == 2) {
            Project project = tvProject.getSelectionModel().getSelectedItem();
            if (project != null) {
            	showProject(project);
            }
        }
    }

    // Hiển thị thông tin chi tiết của project---------------------
    void showProject(Project project) {
    	if( this.paneShowProject.getChildren().size() > 2)
    		this.paneShowProject.getChildren().removeLast();
    	try {
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(getClass().getResource("/view/DetailProject.fxml"));
    		
    		Node nodeFxml = loader.load();
    		__NodeStatic.detailProjectController = (DetailProjectController) loader.getController();
    		__NodeStatic.detailProjectController.setProjectModel(project);
    		
			this.paneShowProject.getChildren().add(nodeFxml);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // Load lại những version chỉnh sửa gần đây----------------
    void  reloadRecent() {
    	vboxRecent.getChildren().clear();
		String sql = "select * from version"
				+ " order by time desc"
				+ " limit 5";
		try {
			ResultSet resultSet = Database.query(sql, null);
			
			while( resultSet.next()) {
				String projectName = resultSet.getString("project");
				String version = resultSet.getString("version");
				
//				System.out.println(projectName + version);
				Version verModel = new Version(
						new Project(projectName),
						version
				);
				
				addRecent(verModel);
			}
			resultSet.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void addRecent(Version verModel) {
		Hyperlink link = new Hyperlink(verModel.getProject().getName() + " >> " + verModel.getVersion());
		link.setFont(Font.font(14));
		link.setUserData(verModel);
		link.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent e) {
		    	showVersion((Version) link.getUserData());
		    }
		});
		vboxRecent.getChildren().add(link);
	}
    
    // Tạo tab mới --- Hiển thị thông tin chi tiết của version ----------------------
    void showVersion(Version version) {
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

	private Tab findTagVersion(Version version) {
		for (Tab tab : __NodeStatic.tabPane.getTabs()) {
            if (tab.getText().equals(version.toString())) {
                return tab;
            }
        }
        return null; 
	}
}
