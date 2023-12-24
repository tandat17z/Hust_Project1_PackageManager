package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Library;
import model.Project;
import model.Version;

public class ProjectController implements Initializable {
	ObservableList<Project> listProject = FXCollections.observableArrayList();
	FilteredList<Project> filteredProject;
	
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// TODO Auto-generated method 
    	colType.setCellValueFactory(new PropertyValueFactory<Project, String>("type"));
    	colName.setCellValueFactory(new PropertyValueFactory<Project, String>("name"));
    	
    	colVersion.setCellValueFactory(new PropertyValueFactory<Version, String>("version"));
    	colTime.setCellValueFactory(new PropertyValueFactory<Version, String>("time"));
    	colDescription.setCellValueFactory(new PropertyValueFactory<Version, String>("description"));
    	
    	tvVersion.setItems(list);
    	reloadProject();
    	reloadRecent();
    }
    
    // Create New project--------------------------------------------
    @FXML
    private Button btnCreateProject;
    
    @FXML
    void openStageCreateProject(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/CreateProject.fxml"));
//			Parent root = FXMLLoader.load(getClass().getResource("/view/CreateProject.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setTitle("Thêm project mới");
			stage.setScene(scene);
			
			CreateProjectController controller = loader.getController();
			controller.setStage(stage);
			
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    
    
    // Reload recent Project------------------------------------------
    @FXML
    private Button btnReload;
    
    @FXML
    private VBox vboxRecent;
    
    @FXML
    private TableView<Project> tvProject;
    
    @FXML
    private TableColumn<Project, String> colType;
    
    @FXML
    private TableColumn<Project, String> colName;
    
    @FXML
    void reload(ActionEvent event) {
    	reloadProject();
    	reloadRecent();
    }
    
    void reloadProject() {
    	// Load Project --------------------------------
    	listProject.clear();
		String sql = "Select * from PROJECT ";

		try {
			Connection connection = Database.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(sql);
			
			ResultSet resultSet = statement.executeQuery();
			while( resultSet.next()) {
				String projectName = resultSet.getString("name");
				String type = resultSet.getString("type");
				String description = resultSet.getString("description");
				String time = resultSet.getString("time");
				Project project = new Project(projectName, type, description, time);
				
				listProject.add(project);
				
			}
			resultSet.close();
			statement.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		filteredProject = new FilteredList<>(listProject);
		tvProject.setItems(filteredProject);
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
    
    void reloadRecent() {
    	vboxRecent.getChildren().clear();
		String sql = "Select * from VERSION "
				+ "order by time DESC "
				+ "limit 4;";

		try {
			Connection connection = Database.getInstance().getConnection();
			Statement statement = connection.createStatement();
			
			ResultSet resultSet = statement.executeQuery(sql);
			
			Boolean first = true;
			while( resultSet.next()) {
				String projectName = resultSet.getString("project");
				String version = resultSet.getString("version");
				
				if( first ) {
					showProject(new Project(projectName));
					first = false;
				}
				
				Version verModel = new Version(
						projectName,
						version
						);
				
				addRecent(verModel);
			}
			resultSet.close();
			statement.close();
			connection.close();
			
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
		    	FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("/view/DependencyTree.fxml"));
				
				Parent root;
				try {
					root = loader.load();
					Scene scene = new Scene(root);
					DpdcTreeController controller = loader.getController();
					controller.setVerModel((Version) link.getUserData());
					
					Stage packageStage = new Stage();
					packageStage.setTitle("Thông tin phụ thuộc");
					packageStage.setScene(scene);
					packageStage.show();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    }
		});
		vboxRecent.getChildren().add(link);
	}
	
    
    // Search Project -------------------------------------------
    @FXML
    private Button btnSearch;
    
    @FXML
    private TextField txtSearch;

    @FXML
    private VBox vboxSearch;
    
    @FXML
    void searchProject(ActionEvent event) {
    	String search = txtSearch.getText();
    	filteredProject.setPredicate(project -> project.getName().contains(search));
    }

    void addSearch(Project project) {
		Hyperlink link = new Hyperlink(project.getName());
		link.setFont(Font.font(14));
		link.setUserData(project);
		link.setOnAction(new EventHandler<ActionEvent>() {
			@Override
		    public void handle(ActionEvent e) {
				showProject((Project) link.getUserData());
		    }
		});
		vboxSearch.getChildren().add(link);
	}
  

	// Tổng quan về Project -------------------------------------------------
    @FXML
	private Label lblType, lblProjectName, lblTime;
	
	@FXML
	private TextArea txtDescription;
	
    @FXML
    private AnchorPane apTree;
    
    @FXML
    private TableView<Version> tvVersion;

    @FXML
    private TableColumn<Version, String> colDescription;

    @FXML
    private TableColumn<Version, String> colTime;

    @FXML
    private TableColumn<Version, String> colVersion;
    
    ObservableList<Version> list = FXCollections.observableArrayList();
    
    @FXML
    void changeDescription(ActionEvent event) {
    	
    }
    
    private void showProject(Project project) {
		lblProjectName.setText(project.getName());
		lblType.setText(project.getType());
		lblTime.setText(project.getTime());
		txtDescription.setText(project.getDescription());
		
		// Hiện thị các version trong table view
		list.clear();
		String sql = "Select * from VERSION "
				+ "where project like ? "
				+ "order by time DESC ";

		try {
			Connection connection = Database.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, project.getName());
			
			ResultSet resultSet = statement.executeQuery();
			while( resultSet.next()) {
				String projectName = resultSet.getString("project");
				String version = resultSet.getString("version");
				
				Version verModel = new Version(
							projectName,
							version
						);
				list.add(verModel);
			}
			resultSet.close();
			statement.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    @FXML
    void showVersionDetail(MouseEvent event) {
    	if (event.getClickCount() == 2) {
            // Lấy đối tượng từ hàng được chọn
            Version verModel = tvVersion.getSelectionModel().getSelectedItem();
            if (verModel != null) {
            	FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("/view/DependencyTree.fxml"));
				
				Parent root;
				try {
					root = loader.load();
					Scene scene = new Scene(root);
					DpdcTreeController controller = loader.getController();
					controller.setVerModel(verModel);
					
					Stage packageStage = new Stage();
					packageStage.setTitle("Thông tin phụ thuộc");
					packageStage.setScene(scene);
					packageStage.show();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
    }
    
}
