package controller;

import java.awt.Desktop;
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
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.Library;

public class LibraryInfoController implements Initializable {
	private String type;
	private String name;
	
	public void setLibrary(String type, String name) {
		this.type = type;
		this.name = name;
		
		show();
	}
	
    @FXML
    private Button btnShow;

    @FXML
    private Hyperlink linkRepo;

    @FXML
    private TreeView<String> tvDependencyTree;

    @FXML
    private TextArea txtGradle;

    @FXML
    private TextArea txtGradleShort;
    
    @FXML
    private TextArea txtMaven;

    @FXML
    private TextArea txtName;

    @FXML
    private TextField txtType;

    @FXML
    private VBox vboxVersion;

    @FXML
    private Label lblVersion;
    
    @FXML
    private Button btnCopyMaven;
    @FXML
    private Button btnCopyGradle;
    @FXML
    private Button btnCopyGradleShort;
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		btnCopyMaven.setOnAction(event -> copyTextToClipboard(txtMaven.getText()));
		btnCopyGradle.setOnAction(event -> copyTextToClipboard(txtGradle.getText()));
		btnCopyGradleShort.setOnAction(event -> copyTextToClipboard(txtGradleShort.getText()));
	}
	
	private void copyTextToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

	void show() {
		txtType.setText(type);
		txtName.setText(name);

		String sql = "Select * from LIBRARY "
				+ "where type = ? and name = ? "
				+ "order by version desc;";
		Connection connection = Database.getInstance().getConnection();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			statement.setString(1, type);
			statement.setString(2, name);
			ResultSet resultSet = statement.executeQuery();
			int cnt = 0;
			while( resultSet.next()) {
				Library libVersion= new Library(
						resultSet.getString("type"),
						resultSet.getString("name"),
						resultSet.getString("version"),
						resultSet.getString("description"),
						resultSet.getString("edit")
				);
				addVersion(libVersion);
				if( cnt == 0 ) showDetail(libVersion);
				cnt += 1;
			}

			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void addVersion(Library library) {
		btnShow.setOnAction(event -> showDir(library));
		Hyperlink link = new Hyperlink(library.getVersion());
		link.setFont(Font.font(14));
		link.setUserData(library);
		link.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent e) {
		    	showDetail(library);
		    }
		});
		vboxVersion.getChildren().add(link);
	}

	private void showDetail(Library library) {
		lblVersion.setText(library.getVersion());
		
		String[] str = library.getName().split(":");
		String groupId = str[0], artifactId = str[1];
		
		showDependencyTree(library);
		String cmdMaven = "<!-- https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "-->\n"
				+ "<dependency>\n"
				+ "    <groupId>" + groupId + "</groupId>\n"
				+ "    <artifactId>" + artifactId + "</artifactId>\n"
				+ "    <version>" + library.getVersion() + "</version>\n"
				+ "</dependency>\n";
		txtMaven.setText(cmdMaven);
		
		String cmdGradle = "<!-- https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "-->\n"
				+ "implementation group: '" + groupId +"', name: '" + artifactId + "', version: '" + library.getVersion() + "'\n";
		txtGradle.setText(cmdGradle);
		
		String cmdGradleShort = "<!-- https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "-->\n"
				+ "implementation '" + library.getName() + ":" + library.getVersion() + "'\n";
		txtGradleShort.setText(cmdGradleShort);
	}
	
	private void showDependencyTree(Library library) {
		// TODO Auto-generated method stub
		File file = new File(library.getPomPath() + "DependencyTree.txt");
		int deep = 1;
		
		TreeItem<String> root = new TreeItem<>(library.toString());
		
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
	
	void showDir(Library library) {
		String directoryPath = library.getPomPath();

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
