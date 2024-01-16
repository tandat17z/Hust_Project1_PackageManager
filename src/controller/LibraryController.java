package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.Library;

public class LibraryController implements Initializable {
	@FXML
	private TextField txtSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private ComboBox<String> cbFiller;
    
    @FXML
    private TableColumn<Library, String> colName;

    @FXML
    private TableColumn<Library, String> colType;

    @FXML
    private TableView<Library> tvLibrary;

    FilteredList<Library> filteredData; // bộ lọc và phải cho table view hiện dữ liệu ở filterData
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
        cbFiller.getItems().addAll("maven", "gradle", "npm", "All");
        
		colType.setCellValueFactory(new PropertyValueFactory<Library, String>("type"));
		colName.setCellValueFactory(new PropertyValueFactory<Library, String>("name"));
    	
    	reloadAll();
	}
	
	// reload để truy xuất lại dữ liệu từ db--------------------------------
	public void reloadAll() {
		cbFiller.setValue("All"); // Giá trị mặc định
		
		filteredData = new FilteredList<>(Library.getAll());
		tvLibrary.setItems(filteredData);
	}
	
	@FXML
	void searchLibrary(ActionEvent event) {
		String search = txtSearch.getText();
		filteredData.setPredicate(library -> library.getName().contains(search));
	}
	
	@FXML
	void fillerType(ActionEvent event) {
		String type = cbFiller.getValue();
        if ("All".equals(type)) {
            filteredData.setPredicate(library-> true); // Hiển thị tất cả
        } else {
            filteredData.setPredicate(library -> library.getType().equals(type));
        }
	}
	
	@FXML
    void showLibraryInfo(MouseEvent event) {
		if (event.getClickCount() == 2) {
            // Lấy đối tượng từ hàng được chọn
            Library library = tvLibrary.getSelectionModel().getSelectedItem();
            if (library != null) {
            	Tab tabLibrary = findTagVersion(library);
            	if( tabLibrary == null ) {
            		tabLibrary = new Tab(library.getType() + ":" + library.getName());
            		tabLibrary.setClosable(true);
            		try {
            			FXMLLoader loader = new FXMLLoader();
            			loader.setLocation(getClass().getResource("/view/DetailLibrary.fxml"));
            			Node fxmlLoader = loader.load();
            			DetailLibraryController controller = loader.getController();
            			controller.setLibrary(library);
            			
            			tabLibrary.setContent(fxmlLoader);
            			__NodeStatic.tabPane.getTabs().add(tabLibrary);
            		} catch (IOException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            	}
            	__NodeStatic.tabPane.getSelectionModel().select(tabLibrary);
            }
        }
    }
	
	private Tab findTagVersion(Library library) {
		for (Tab tab : __NodeStatic.tabPane.getTabs()) {
            if (tab.getText().equals(library.toString())) {
                return tab;
            }
        }
        return null; 
	}
}
