package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import model.Version;

public class DpdcTreeController implements Initializable {
	Version verModel;
	String projectName;
	String version;
	
	public void setVerModel(Version verModel) {
		this.verModel = verModel;
		this.projectName = verModel.getProject().getName();
		this.version = verModel.getVersion();
		
		showDependencyTree();
	}
	
    @FXML
    private Label lblName;

    @FXML
    private Label lblVersion;

    @FXML
    private TreeView<String> tvDependencyTree;

    @FXML
    void selectTree(MouseEvent event) {

    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
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

}
