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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Change;
import model.Library;
import model.Project;
import model.Version;

public class ProjectController implements Initializable {
	Project curr;
	
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
    	
    	// Hiển thị ttvRecentHistory
		colTimeHis.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<Change, String> arg) -> new SimpleStringProperty(arg.getValue().getValue().getTime()));
		colTypeHis.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<Change, String> arg) -> new SimpleStringProperty(arg.getValue().getValue().getType()));
		colDetailHis.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Change, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Change, String> param) {
				// Sử dụng SimpleStringProperty thay vì SimpleObjectProperty
				return new SimpleStringProperty(param.getValue().getValue().getDetail());
			}
		});
    			
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
				loader.setLocation(getClass().getResource("/view/Version.fxml"));
				
				Parent root;
				try {
					root = loader.load();
					Scene scene = new Scene(root);
					VersionController controller = loader.getController();
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
    
    @FXML
    private Button btnVersion;
    
    @FXML
    private TreeTableView<Change> ttvRecentHis;
    
    @FXML
    private TreeTableColumn<Change, String> colTimeHis;
    
    @FXML
    private TreeTableColumn<Change, String> colTypeHis;
    
    @FXML
    private TreeTableColumn<Change, String> colDetailHis;
    
    ObservableList<Version> list = FXCollections.observableArrayList();
    
    private void showProject(Project project) {
    	curr = project;
    	showRecentHistory(); // Hiển thị lần sửa gần nhất
    	
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
    
    void showRecentHistory() {
    	// Lấy ra Change của mới nhất của project
    	try {
    		ttvRecentHis.setRoot(null);
    	}
    	catch(Exception e){
    		System.out.println("root = null");
    	}
		String sql = "select * from CHANGE "
				+ "where version_id In (SELECT version_id From VERSION "
									+ " WHERE project = ? ) "
				+ "order by time desc;";
		
		Connection connection = Database.getInstance().getConnection();
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1,  curr.getName());
			ResultSet resultSet = statement.executeQuery();
			
			String oldTime = "";
			int cnt = 0;
			TreeItem<Change> root = null;
			while( resultSet.next()) {
				int version_id = resultSet.getInt("version_id");
				String type = resultSet.getString("type");
				String time = resultSet.getString("time");
				String detail = resultSet.getString("detail");
			
				// link tới version này
				btnVersion.setText( new Version(version_id).getVersion());
				if( !oldTime.equals(time) && cnt == 0) {
					root = new TreeItem<>(new Change(1, time, "", ""));
					oldTime = time;
				}
				
				if( oldTime.equals(time)) {
					root.getChildren().add(new TreeItem<>(new Change(1, "", type, detail)));
					cnt += 1;
				}
				else break;
			}
			
			if( root != null) {
				ttvRecentHis.setRoot(root);
				ttvRecentHis.setShowRoot(true);
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
				loader.setLocation(getClass().getResource("/view/Version.fxml"));
				
				Parent root;
				try {
					root = loader.load();
					Scene scene = new Scene(root);
					VersionController controller = loader.getController();
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
    
    @FXML
	void updateDescription(ActionEvent event) {
		curr.update(txtDescription.getText());
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Cập nhật mô tả");
    	alert.setHeaderText(curr.getName());
    	alert.setContentText("Cập nhật thành công description");
    	alert.show();
	}
}
