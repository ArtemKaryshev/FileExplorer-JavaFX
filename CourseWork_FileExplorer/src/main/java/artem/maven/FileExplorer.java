package artem.maven;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * The main class of the FileExplorer application, representing the interface for working with files and directories.
 */
public class FileExplorer extends Application {
    private static final Logger logger = LogManager.getLogger(FileExplorer.class);

    private Functions functions = new Functions();

    /**
     * The method launching the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        logger.debug("The application has successfully finished its work \n");
    }

    /**
     * A method allowing to launch the application, using JavaFX.
     *
     * @param primaryStage the window
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("File Explorer");

            String iconPath = "/icon/case-file.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            primaryStage.getIcons().add(icon);

            TableColumn<FileItem, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setPrefWidth(200);

            TableColumn<FileItem, String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeColumn.setPrefWidth(100);

            TableColumn<FileItem, Long> sizeColumn = new TableColumn<>("Size");
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size_res"));
            sizeColumn.setPrefWidth(160);

            TableColumn<FileItem, String> fileOwnerColumn = new TableColumn<>("Owner");
            fileOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
            fileOwnerColumn.setPrefWidth(130);

            TableColumn<FileItem, String> dateColumn = new TableColumn<>("Created");
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            dateColumn.setPrefWidth(180);

            functions.table.getColumns().addAll(nameColumn, typeColumn, sizeColumn, fileOwnerColumn, dateColumn);

            Label title = new Label("Push button 'Select Directory'");
            title.setStyle("-fx-font-size: 14; -fx-wrap-text:true; -fx-border-color:black;");
            title.setPadding(new Insets(10));
            title.setAlignment(Pos.CENTER);
            title.setMaxWidth(Double.MAX_VALUE);

            Button selectDriveButton = new Button("Select Directory");
            selectDriveButton.setPrefWidth(140);
            selectDriveButton.setOnAction(e -> {
                try {
                    functions.selectDrive(primaryStage, title);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });


            Button backButton = new Button("Back");
            backButton.setPrefWidth(100);
            backButton.setOnAction(e -> {
                try {
                    functions.goBack(title);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            Button calcFolderSize = new Button("Show Folder Size");
            calcFolderSize.setPrefWidth(130);
            calcFolderSize.setOnAction(e -> {
                try {
                    functions.calcSizeFunc(functions.currentDirectoryPath, title);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            Region spacer1 = new Region();
            spacer1.setPrefWidth(215);

            Region spacer2 = new Region();
            spacer2.setPrefWidth(185);

            VBox vbox = new VBox();
            HBox hBox = new HBox();
            hBox.getChildren().addAll(backButton, spacer1, selectDriveButton, spacer2, calcFolderSize);
            vbox.getChildren().addAll(functions.table, hBox, title);

            Scene scene = new Scene(vbox, 770, 451);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            logger.debug("The application has been launched successfully");

        } catch (Exception ex) {
            logger.error("Error launching the application", ex);
        }
    }
}
