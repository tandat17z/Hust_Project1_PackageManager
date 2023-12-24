package controller;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class test extends Application {

	public class Person {

	    private final SimpleStringProperty firstName;
	    private final SimpleStringProperty lastName;
	    private final SimpleIntegerProperty age;

	    public Person(String firstName, String lastName, int age) {
	        this.firstName = new SimpleStringProperty(firstName);
	        this.lastName = new SimpleStringProperty(lastName);
	        this.age = new SimpleIntegerProperty(age);
	    }

	    public String getFirstName() {
	        return firstName.get();
	    }

	    public SimpleStringProperty firstNameProperty() {
	        return firstName;
	    }

	    public void setFirstName(String firstName) {
	        this.firstName.set(firstName);
	    }

	    public String getLastName() {
	        return lastName.get();
	    }

	    public SimpleStringProperty lastNameProperty() {
	        return lastName;
	    }

	    public void setLastName(String lastName) {
	        this.lastName.set(lastName);
	    }

	    public int getAge() {
	        return age.get();
	    }

	    public SimpleIntegerProperty ageProperty() {
	        return age;
	    }

	    public void setAge(int age) {
	        this.age.set(age);
	    }
	}
	
    private final ObservableList<Person> originalData = FXCollections.observableArrayList(
            new Person("John", "Doe", 25),
            new Person("Jane", "Doe", 30),
            new Person("Bob", "Smith", 22)
    );

    private final FilteredList<Person> filteredData = new FilteredList<>(originalData);

    @Override
    public void start(Stage primaryStage) {
        TableView<Person> tableView = new TableView<>(filteredData);

        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
        TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");
        TableColumn<Person, Integer> ageColumn = new TableColumn<>("Age");

        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        ageColumn.setCellValueFactory(cellData -> cellData.getValue().ageProperty().asObject());

        tableView.getColumns().addAll(firstNameColumn, lastNameColumn, ageColumn);

        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("John", "Jane", "Bob", "All"); // Các giá trị bạn muốn lọc
        filterComboBox.setValue("All"); // Giá trị mặc định

        filterComboBox.setOnAction(event -> {
            String selectedName = filterComboBox.getValue();
            if ("All".equals(selectedName)) {
                filteredData.setPredicate(person -> true); // Hiển thị tất cả
            } else {
                filteredData.setPredicate(person -> selectedName.equals(person.getFirstName()));
            }
        });


        VBox vbox = new VBox(filterComboBox, tableView);

        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TableView Filter Example");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
