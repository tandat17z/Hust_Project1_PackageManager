package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import database.Database;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Change;
import model.Library;
import model.Root;
import model.Version;

public class DetailVersionController implements Initializable {
	Version verModel;
	String projectName;
	String version;
	
	public void setVerModel(Version verModel) {
		this.verModel = verModel;
		this.projectName = verModel.getProject().getName();
		this.version = verModel.getVersion();
		
		init();
	}
	
	void init() {
		if( verModel.getProject().getType().equals("gradle")) {
			paneBg.setStyle("-fx-background-color: #ffe169");
		}
		lblName.setText(projectName);
		lblVersion.setText(version);
		txtDescription.setText(verModel.getDescription());
		
		showDependencyTree();
		showHistory();
		statistics();
	}
	
	private void statistics() {
		dpdcNum.setText( Integer.toString(verModel.getDependencyDepth1().size()) );
		
		int [] num = verModel.getRecentChange();
		addNum.setText(Integer.toString( num[0]));
		delNum.setText(Integer.toString( num[1]));
		updateNum.setText(Integer.toString( num[2]));
	}
	@FXML
	private AnchorPane paneBg;
    @FXML
    private Button btnChange;

    @FXML
    private Button btnReload;

    @FXML
    private Button btnUpdate;

    @FXML
    private TreeTableView<Change> ttvHistory;
    
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
    private TextArea txtDescription;

    @FXML
    private Label addNum, delNum, updateNum, dpdcNum;
    
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
    
    @FXML
    void reload(ActionEvent event) {
    	init();
    }

    @FXML
    void selectTree(MouseEvent event) {

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
    void updateDescription(ActionEvent event) {
    	verModel.setDescription(txtDescription.getText());
    	verModel.save(true);
    	
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Cập nhật mô tả");
    	alert.setHeaderText(verModel.getProject().getName() + ":" + verModel.getVersion());
    	alert.setContentText("Cập nhật thành công description");
    	alert.show();
    }
    
    // Hàm hiển thị cây phụ thuộc ------------------------------
    void showDependencyTree() {
    	Map<Library, Library> tree = Root.getInstance(verModel.getProject().getType()).getDependency(verModel.getVersionPath());
    	TreeItem<String> root = new TreeItem<>(projectName + ":" + version);
		
    	Map<Library, TreeItem<String> > map = new TreeMap<>();
    	
    	// Duyệt danh sách cha của cây 
    	for( Library lib: tree.keySet()) {
    		// lấy nhánh của lib hiện tại
    		TreeItem<String> branch = map.get(lib);
    		if( branch == null) {
    			branch = new TreeItem<>(lib.getDescription());
    			map.put(lib, branch);
    		}
    		
    		//Lấy nhánh cha của lib và nối nhánh con vào
    		Library libPa = tree.get(lib);
    		if( libPa == null) {
    			root.getChildren().add(branch);
    		}
    		else {
    			TreeItem<String> pa = map.get(libPa);
    			if( pa == null) {
    				pa = new TreeItem<>(libPa.getDescription());
    				map.put(libPa, pa);
    			}
    			pa.getChildren().add(branch);
    		}
    	}
    	tvDependencyTree.setRoot(root);
    }
    

    void showHistory() {
		String sql = "select * from CHANGE "
				+ "where version_id = ?"
				+ "order by time desc;";
		List<Object> arg = new ArrayList<>();
		arg.add(verModel.getVersionId());
		
		try {
			ResultSet resultSet = Database.query(sql, arg);
			
			TreeItem<Change> root = new TreeItem<>(new Change(-1, "", "", ""));
			TreeItem<Change> parent = null;
			String oldTime ="";
			while( resultSet.next()) {
				String time = resultSet.getString("time");
				String type = resultSet.getString("type");
				String detail = resultSet.getString("detail");
				
				if( !oldTime.equals(time) ) {
					if( parent != null) root.getChildren().add(parent);
					parent = new TreeItem<>(new Change(-1, time, "", ""));
					oldTime = time;
				}
				TreeItem<Change> child = new TreeItem<>(new Change(-1, "", type, detail));
				parent.getChildren().add(child);
			}
			root.getChildren().add(parent);
			ttvHistory.setRoot(root);
			ttvHistory.setShowRoot(false);
			
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    void showConfigFileDir(ActionEvent event) {
    	String directoryPath = verModel.getVersionPath();

        // Kiểm tra xem Desktop được hỗ trợ trên hệ thống không
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File directory = new File(directoryPath);

            // Kiểm tra xem thư mục tồn tại không
            if (directory.exists() && directory.isDirectory()) {
                try {
                    // Mở thư mục
                    desktop.open(directory);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Thư mục không tồn tại.");
            }
        } else {
            System.out.println("Desktop không được hỗ trợ trên hệ thống này.");
        }
    }
}
