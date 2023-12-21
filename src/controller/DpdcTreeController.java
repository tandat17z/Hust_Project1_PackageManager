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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import model.MavenProject;
import model.Project;

public class DpdcTreeController implements Initializable {
	Project project;
	String name;
	String version;
	
	public void setProject(Project project) {
		this.project = project;
		this.name = project.getName();
		this.version = project.getVersion();
		
		printTree();
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
		// Sự kiện theo dõi khi một TreeItem được chọn
        tvDependencyTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Khi một TreeItem được chọn, thêm sự kiện xử lý phím Enter
                tvDependencyTree.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        // Thực hiện các hành động cần thiết với TreeItem đã được chọn
                        System.out.println("Enter key pressed on: " + newValue.getValue());
                    }
                });
            }
        });
	}
	
	void printTree() {
		System.out.println("initTree");
		lblName.setText(name);
		lblVersion.setText(version);
		// TODO Auto-generated method stub
		File file = new File(MavenProject.projectDir + "\\" + name + "\\" + version + "\\dependencyTree.txt");
		int deep = 1;
		
		TreeItem<String> root = new TreeItem<>(name + ":" + version);
		
		List<TreeItem<String>> pa  = new ArrayList(); //List parent cho các mức
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
			    
				if(data.equals("[INFO] com.packagemanager:mavenProject:jar:1.0-SNAPSHOT"))
			    	check = true;
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
