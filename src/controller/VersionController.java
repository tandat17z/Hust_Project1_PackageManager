package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.Database;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Change;
import model.Project;
import model.Version;
import javafx.beans.property.SimpleStringProperty;

public class VersionController implements Initializable {
	Version verModel;
	String projectName;
	String version;
	
	public void setVerModel(Version verModel) {
		this.verModel = verModel;
		this.projectName = verModel.getProject().getName();
		this.version = verModel.getVersion();
		
		showDependencyTree();
		reload();
	}
	
	@FXML
    private Button btnChange;

    @FXML
    private Button btnUpdate;

    @FXML
    private TreeTableColumn<Change, String> colDetail;

    @FXML
    private TreeTableColumn<Change, String> colTime;

    @FXML
    private TreeTableColumn<Change, String> colType;


    @FXML
    private Label lblName;

    @FXML
    private Label lblVersion;

    @FXML
    private TreeView<String> tvDependencyTree;

    @FXML
    private TreeTableView<Change> ttvHistory;

    @FXML
    private TextArea txtDescription;


    @FXML
    void selectTree(MouseEvent event) {

    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Hiển thị ttvHistory
		colTime.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<Change, String> arg) -> new SimpleStringProperty(arg.getValue().getValue().getTime()));
		colType.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<Change, String> arg) -> new SimpleStringProperty(arg.getValue().getValue().getType()));
		colDetail.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Change, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Change, String> param) {
				// Sử dụng SimpleStringProperty thay vì SimpleObjectProperty
				return new SimpleStringProperty(param.getValue().getValue().getDetail());
			}
		});
		// Ấn Enter để tới library trong tree
        tvDependencyTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tvDependencyTree.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        System.out.println("Enter key pressed on: " + newValue.getValue());
                        
                        
                        
                        
                    }
                });
            }
        });
	}
	
	void reload() {
		txtDescription.setText(verModel.getDescription());
		String sql = "select * from CHANGE "
				+ "where version_id = ?"
				+ "order by time desc;";
		
		Connection connection = Database.getInstance().getConnection();
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1,  verModel.getVersionId());
			ResultSet resultSet = statement.executeQuery();
			
			TreeItem<Change> root = new TreeItem<>(new Change(1, "", "", ""));
			Boolean checkParent = true;
			TreeItem<Change> parent = null;
			String oldTime ="";
			while( resultSet.next()) {
				String time = resultSet.getString("time");
				String type = resultSet.getString("type");
				String detail = resultSet.getString("detail");
				
				if( !oldTime.equals(time) ) {
					if( parent != null) root.getChildren().add(parent);
					parent = new TreeItem<>(new Change(1, time, "", ""));
					oldTime = time;
				}
				TreeItem<Change> child = new TreeItem<>(new Change(1, "", type, detail));
				parent.getChildren().add(child);
			}
			root.getChildren().add(parent);
			ttvHistory.setRoot(root);
			ttvHistory.setShowRoot(false);
			
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		TreeItem<Change> row1 = new TreeItem<>(new Change(1, "", "remove", "org.apache.httpcomponents:httpclient:4.5.14"));
//		TreeItem<Change> row2 = new TreeItem<>(new Change(1, "", "remove", "org.apache.httpcomponents:httpclient:4.5.14"));
//		TreeItem<Change> row3 = new TreeItem<>(new Change(1, "", "remove", "org.apache.httpcomponents:httpclient:4.5.14"));
//		
//		TreeItem<Change> parent = new TreeItem<>(new Change(1, "2023-12-27 01:13:00", "", ""));
//		TreeItem<Change> parent1 = new TreeItem<>(new Change(1, "2023-12-27 01:13:00", "", ""));
//		
//		TreeItem<Change> root = new TreeItem<>(new Change(1, "2023-12-27 01:13:00", "", ""));
//		parent.getChildren().addAll(row1, row2);
//		parent1.getChildren().addAll(row3);
//		root.getChildren().addAll(parent, parent1);
//		
//		ttvHistory.setRoot(root);
//		ttvHistory.setShowRoot(false);
		
	}
	
	void showDependencyTree() {
		lblName.setText(projectName);
		lblVersion.setText(version);
		// TODO Auto-generated method stub
		File file = new File(verModel.getVersionDir() + "DependencyTree.txt");
		int deep = 1;
		
		TreeItem<String> root = new TreeItem<>(projectName + ":" + version);
		
		List<TreeItem<String>> pa  = new ArrayList<TreeItem<String>>(); //List parent cho các mức
		pa.add(root);
		
		Scanner myReader;
		try {
			myReader = new Scanner(file);
			boolean check = false; // Bắt đầu tìm đến đoạn text chứa thông tin cây
			
			while ( myReader.hasNextLine() ) { 
			    String data = myReader.nextLine();
			    if(check) {
			    	while( deep != 0) {
			    		String str = "\\[INFO\\] .{" + (deep*3 - 1) + "}\\s(.+)";
			    		Pattern pattern = Pattern.compile(str);
			    		Matcher matcher = pattern.matcher(data);
			    		if( matcher.find()) {
			    			String packageName = matcher.group(1);
//			    			String[] info = matcher.group(1).split(":");
			    			TreeItem<String> branchItem = new TreeItem<>(packageName);
			    			
			    			pa.get(deep-1).getChildren().add(branchItem); // Gán cho cho nút hiện tại
			    			pa.add(branchItem); // Thêm cha mới vào list parent
			    			deep += 1;
			    			break;
			    		}
			    		deep -= 1;
			    		pa.removeLast();
			    		
			    		if(deep == 0) tvDependencyTree.setRoot(root);
			    	}
		    	}
			    if (deep == 0) break;
			    
				if(data.equals("[INFO] com.packagemanager:MavenProjectRoot:jar:1.0-SNAPSHOT"))
			    	check = true;
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@FXML
	void updateConfigFile(ActionEvent event) {
		try {
    		FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/EditConfigFile.fxml"));
//			Parent root = FXMLLoader.load(getClass().getResource("/view/CreateProject.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setTitle("Chỉnh sửa file cấu hình");
			stage.setScene(scene);
			
			EditConfigFileController controller = loader.getController();
			controller.setVerModel(stage, verModel);
			
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
    void reload(ActionEvent event) {
		showDependencyTree();
		reload();
    }
	
	@FXML
	void updateDescription(ActionEvent event) {
		verModel.update(txtDescription.getText());
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Cập nhật mô tả");
    	alert.setHeaderText(verModel.getProject().getName() + ":" + verModel.getVersion());
    	alert.setContentText("Cập nhật thành công description");
    	alert.show();
	}


}
