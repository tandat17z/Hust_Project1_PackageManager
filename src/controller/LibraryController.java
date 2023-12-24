package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Library;

public class LibraryController implements Initializable {
	@FXML
	private TextField txtSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private ComboBox<String> cbFiller;
    
    @FXML
    private TableColumn<Library, String> colDescription;

    @FXML
    private TableColumn<Library, String> colName;

    @FXML
    private TableColumn<Library, String> colType;

    @FXML
    private TableColumn<Library, String> colVersion;

    @FXML
    private TableView<Library> tvLibrary;


    ObservableList<Library> list = FXCollections.observableArrayList();

    FilteredList<Library> filteredData; // bộ lọc và phải cho table view hiện dữ liệu ở filterData
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
        cbFiller.getItems().addAll("maven", "gradle", "npm", "All");
        
		colType.setCellValueFactory(new PropertyValueFactory<Library, String>("type"));
		colName.setCellValueFactory(new PropertyValueFactory<Library, String>("name"));
		colVersion.setCellValueFactory(new PropertyValueFactory<Library, String>("version"));
    	colDescription.setCellValueFactory(new PropertyValueFactory<Library, String>("description"));
    	
    	reloadAll();
	}
	
	// reload để truy xuất lại dữ liệu từ db--------------------------------
	@FXML
	void reload(ActionEvent event) {
		reloadAll();
	}
	
	private void reloadAll() {
		cbFiller.setValue("All"); // Giá trị mặc định
		
		list.clear();
		String sql = "Select * from LIBRARY ";

		try {
			Connection connection = Database.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(sql);
			
			ResultSet resultSet = statement.executeQuery();
			while( resultSet.next()) {
				String type = resultSet.getString("type");
				String name = resultSet.getString("name");
				String version = resultSet.getString("version");
				String description = resultSet.getString("description");
				String edit = resultSet.getString("edit");
				
				Library library = new Library(
							type,
							name,
							version,
							description,
							edit
						);
				list.add(library);
			}
			
			resultSet.close();
			statement.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		filteredData = new FilteredList<>(list);
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
}
