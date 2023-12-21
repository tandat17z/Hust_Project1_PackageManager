package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.MavenProject;
import model.Project;

public class MainController extends Method implements Initializable{
	static Stage stage_NewProject;
	
    @FXML
    private AnchorPane apTree;

    @FXML
    private Button btnNewProject;

    @FXML
    private Button btnSearch;

    @FXML
    private ComboBox<?> cbFiller;

    @FXML
    private Label lblProjectName;

    @FXML
    private TreeView<String> tvDir;
    
    public void selectItem() {
    	
    }

    @FXML
    private TextField txtSearch;

    @FXML
    private VBox vboxRecent;
    
    @FXML
    private Button btnReload;
    
    @FXML
    private VBox vboxSearch;
    
    @FXML
    void seachAction(ActionEvent event) {

    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		reload();
	}

	@FXML
	void toSceneCreateProject(ActionEvent event) {
		stage_NewProject = newStage("NewProject", "Tạo project mới");
	}
	
	@FXML
	void reloadRecentProject(ActionEvent event) {
		reload();
	}
	
	void reload() {
		vboxRecent.getChildren().clear();
		String sql = "Select * from PROJECT "
				+ "order by time DESC "
				+ "limit 5;";

		try {
			Connection connection = DatabaseConnection.getInstance().getConnection();
			Statement statement = connection.createStatement();
			
			ResultSet resultSet = statement.executeQuery(sql);
			while( resultSet.next()) {
				Project project = new Project(
						resultSet.getString("name"),
						resultSet.getString("version"),
						resultSet.getString("type"),
						resultSet.getString("description"),
						resultSet.getString("time")
						);
				add(project, vboxRecent);
			}
			resultSet.close();
			statement.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("init");
	}
	
	void add(Project project, VBox vbox) {
		Hyperlink link = new Hyperlink(project.getName() + " >> " + project.getVersion());
		link.setFont(Font.font(14));
		link.setUserData(project);
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
					controller.setProject((Project) link.getUserData());
					
					System.out.println(link.getText());
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
		vbox.getChildren().add(link);
	}
	
	@FXML
	void searchProject(ActionEvent event) {
		vboxSearch.getChildren().clear();
		String sql = "Select * from PROJECT "
				+ "where name like ? "
				+ "order by time DESC ";

		try {
			Connection connection = DatabaseConnection.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, "%" + txtSearch.getText() + "%");
			
			ResultSet resultSet = statement.executeQuery();
			while( resultSet.next()) {
				Project project = new Project(
						resultSet.getString("name"),
						resultSet.getString("version"),
						resultSet.getString("type"),
						resultSet.getString("description"),
						resultSet.getString("time")
						);
				add(project, vboxSearch);
			}
			resultSet.close();
			statement.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("search");
	}
}
