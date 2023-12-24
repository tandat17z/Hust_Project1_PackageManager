package controller;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Project;
import model.Version;

public class CreateProjectController implements Initializable {
	private Stage stage;
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	private File configFile = null;
	
    @FXML
    private Button btnCancel, btnCreate;

    @FXML
    private RadioButton btnGradle, btnMaven, btnNpm;

    @FXML
    private Button btnSelectFile;

    @FXML
    private Label lblFile, lblVersion;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtName;

    @FXML
    private ToggleGroup type;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// TODO Auto-generated method stub
    	btnCreate.setDisable(true);
    }
    
    @FXML
    void createProject(ActionEvent event) {
    	String name = txtName.getText();
    	String type = getType();
    	String time = getTime();
    	String description = txtDescription.getText();
    	
    	// Lưu project
    	Project newProject = new Project(name, type, description, time);
    	newProject.save();
    	
    	// Lưu version đầu tiên
    	String version = lblVersion.getText();
    	Version versionModel = new Version(newProject, version, "create new project", time);
    	versionModel.save(configFile);
    	
    	this.stage.close();
    	
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle(type);
    	alert.setHeaderText(name + ": " + version);
    	alert.setContentText("Tạo project mới thành công lúc " + getTime());
    	alert.show();
    }

	@FXML
    void addConfigFile(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(new File("C:\\Users\\tandat17z\\eclipse-workspace"));
    	configFile = fileChooser.showOpenDialog(null);
    	if( configFile.getName().equals("pom.xml") == false) {
    		configFile = null;
    		lblFile.setText("Không đúng định dạng config file");
    	}
    	else {
    		lblFile.setText(configFile.getPath());	
    		lblVersion.setText(getVersion());
    	}
    }

    @FXML
    void keyReleased(KeyEvent event) {
    	Object source = event.getSource();
    	if(((TextInputControl) source).getText().length() != 0) 
    		checkCreate();
    	else
    		btnCreate.setDisable(true);
    }

    @FXML
    void mouseReleased(MouseEvent event) {
    	checkCreate();
    }
    
    private void checkCreate() {
    	String name = txtName.getText();
    	String type = getType();
    	String time = getTime();
    	String description = txtDescription.getText();
    	
    	Project newProject = new Project(name, type, description, time);
    	
    	if( !newProject.exists() && configFile != null ) btnCreate.setDisable(false);
    	else btnCreate.setDisable(true);
	}
	
    private String getType() {
    	if(btnGradle.isSelected())
    		return "gradle";
    	else if(btnNpm.isSelected())
    		return "npm";
		return "maven";
    }
    
    private String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date currentDate = new Date();
    	return dateFormat.format(currentDate);
	}
    
    private String getVersion() {
    	try {
            // Tạo một đối tượng DocumentBuilderFactory để tạo ra một DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Đọc file pom.xml
            Document doc = builder.parse(configFile);

            // Lấy ra phần tử root của XML (thường là <project>)
            Element rootElement = doc.getDocumentElement();

            // Lấy ra danh sách các phần tử con của root có tên là "version"
            NodeList versionNodes = rootElement.getElementsByTagName("version");

            // Lấy ra giá trị của phần tử version đầu tiên (giả sử chỉ có một version)
            if (versionNodes.getLength() > 0) {
                Node versionNode = versionNodes.item(0);
                String version = versionNode.getTextContent();
                return version;
                
            } else {
                System.out.println("Không tìm thấy phần tử version trong file pom.xml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}
    
    @FXML
    void cancelStage(ActionEvent event) {
    	this.stage.close();
    }
}
