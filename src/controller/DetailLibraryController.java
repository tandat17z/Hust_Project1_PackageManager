package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.Library;
import model.Root;

public class DetailLibraryController {
	private String type;
	private String name;
	private Library curLibrary;
	private String url;
	
	public void setLibrary(Library library) {
		this.type = library.getType();
		this.name = library.getName();
		
		if(this.type.equals("maven"))
			this.url = "https://mvnrepository.com/artifact/" + this.name.replace(':', '/');
		else if(this.type.equals("npm"))
			this.url = "https://www.npmjs.com/package/" + this.name.replace(':', '/');
		curLibrary = library;
		showDetailLibrary();
		init();
	}
	
	private void init() {
		txtType.setText(type);
		txtName.setText(name);

		for(Library lib: curLibrary.getVersionOfLibrary()) {
			addVersion(lib);
		}
	}
	
	void addVersion(Library library) {
		Hyperlink link = new Hyperlink(library.getVersion());
		link.setFont(Font.font(14));
		link.setUserData(library);
		link.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent e) {
		    	curLibrary = library;
		    	showDetailLibrary();
		    }
		});
		vboxVersion.getChildren().add(link);
	}
	
	private void showDetailLibrary() {
		txtVersion.setText(curLibrary.getVersion());
		txtDescription.setText(curLibrary.getDescription());
		
		showDependencyTree();
		if( !curLibrary.getType().equals("npm") ) {
			tabPaneTree.getTabs().remove(tabNpm);
			String[] str = curLibrary.getName().split(":");
			String groupId = str[0], artifactId = str[1];
			String cmdMaven = "<!-- https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "-->\n"
					+ "<dependency>\n"
					+ "    <groupId>" + groupId + "</groupId>\n"
					+ "    <artifactId>" + artifactId + "</artifactId>\n"
					+ "    <version>" + curLibrary.getVersion() + "</version>\n"
					+ "</dependency>\n";
			txtMaven.setText(cmdMaven);
			
			String cmdGradle = "// https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "\n"
					+ "implementation '" + groupId +":" + artifactId + ":" + curLibrary.getVersion() + "'\n";
			txtGradle.setText(cmdGradle);
			
		}
		else {
			tabPaneTree.getTabs().remove(tabMaven);
			tabPaneTree.getTabs().remove(tabGradle);
			String cmdNpm = "npm install " + curLibrary.getName();
			txtNpm.setText(cmdNpm);
		}
	}
	
    private void showDependencyTree() {
    	File file = new File(curLibrary.getLibraryPath() + "DependencyTree.txt");
    	if ( !file.exists()) {
            System.out.println("---ERROR ---DetailLibrary: Chưa tồn tại file dependencyTree");
            return;
    	}
    	
    	Map<Library, Library> tree = Root.getInstance(curLibrary.getType()).getDependency(curLibrary.getLibraryPath());
    	TreeItem<String> root = new TreeItem<>(curLibrary.getName() + ":" + curLibrary.getVersion());
		
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

	@FXML
    private Button btnConfigFile;

    @FXML
    private TreeView<String> tvDependencyTree;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Tab tabGradle;

    @FXML
    private Tab tabMaven;

    @FXML
    private Tab tabNpm;

    
    @FXML
    private TextArea txtMaven;

    @FXML
    private TextArea txtGradle;
    
    @FXML
    private TextArea txtNpm;
    
    @FXML
    private TextField txtName;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtVersion;

    @FXML
    private VBox vboxVersion;
    
    @FXML
    private TabPane tabPaneTree;
    
    @FXML
    void showConfigFileDir(ActionEvent event) {
    	String directoryPath = curLibrary.getLibraryPath();

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
    

	@FXML
    void copyText(MouseEvent event) {
		TextArea sourceTextArea = (TextArea) event.getSource();
        String content = sourceTextArea.getText();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Copy Success");
        alert.setHeaderText(null);
        alert.setContentText("Content copied successfully!");
        alert.showAndWait();
    }
	
	@FXML
    void updateDescription(ActionEvent event) {
		curLibrary.setDescription(txtDescription.getText());
		curLibrary.save(true);
		
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Cập nhật mô tả");
    	alert.setHeaderText(curLibrary.getName() + ":" + curLibrary.getVersion());
    	alert.setContentText("Cập nhật thành công description");
    	alert.show();
    }

	@FXML
    void onBrower(ActionEvent event) {
		try {
            // Kiểm tra xem máy tính có hỗ trợ Desktop không
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                // Kiểm tra xem URI có hợp lệ không
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
