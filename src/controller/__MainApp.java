package controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class __MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button openBrowserButton = new Button("Mở trình duyệt");

        openBrowserButton.setOnAction(e -> {
            // URL bạn muốn mở
            String url = "https://youtube.com";

            // Mở trình duyệt mặc định với URL đã cho
            openBrowser(url);
        });

        StackPane root = new StackPane();
        root.getChildren().add(openBrowserButton);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("JavaFX - Mở Trình Duyệt");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openBrowser(String url) {
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

    public static void main(String[] args) {
        launch(args);
    }
}
