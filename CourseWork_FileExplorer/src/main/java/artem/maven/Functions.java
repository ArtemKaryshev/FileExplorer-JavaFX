package artem.maven;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class with core functions for handling files and displaying data in the application.
 */
public class Functions {
    private static final Logger logger = LogManager.getLogger(Functions.class);
    private boolean inDisk = false;
    protected final TableView<FileItem> table = new TableView<>();
    protected String currentDirectoryPath = "";
    private long time;
    private String size_res;
    File selectedFile;

    /**
     * Selects a disk or directory.
     *
     * @param primaryStage the primary stage of the application
     * @param title        the label to display the path of the selected directory
     * @throws IOException if an I/O error occurs
     */
    protected void selectDrive(Stage primaryStage, Label title) throws IOException {
        count_of_back = 0;
        i = 3;
        inDisk = false;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            currentDirectoryPath = selectedDirectory.getAbsolutePath();
            displayFiles(currentDirectoryPath, title);
        }
    }

    /**
     * Displays files in the specified directory.
     *
     * @param directoryPath the path to the directory
     * @param title         the label to display the path of the directory
     * @throws IOException if an I/O error occurs
     */
    protected void displayFiles(String directoryPath, Label title) throws IOException {
        table.getItems().clear();
        File directory = new File(directoryPath);
        title.setText("'" + directoryPath + "'");

        for (File file : directory.listFiles()) {
            double sizeFile;
            long fileLength = file.length();
            if (fileLength < 1000) {
                sizeFile = fileLength;
                size_res = (int) sizeFile + " bytes";
            } else if (1000 <= fileLength && fileLength < 100000) {
                sizeFile = fileLength / (Math.pow(2, 10));
                size_res = String.format("%.1f", sizeFile) + " Kb";
            } else {
                sizeFile = fileLength / (Math.pow(2, 20));
                size_res = String.format("%.1f", sizeFile) + " Mb";
            }

            String name = file.getName();
            String type = file.isDirectory() ? "Folder" : "File";

            String size = file.isDirectory() ? "0" : size_res;
            String owner = owner(file.getAbsolutePath());
            String date = getFormattedDate(file.lastModified());
            table.getItems().add(new FileItem(name, type, size, owner, date));
        }
        mouseClick(directory, title);
    }

    /**
     * Handles mouse click to display file information or navigate to a new directory.
     *
     * @param directoryPath the path to the directory
     * @param title         the label to display file or directory information
     */
    protected void mouseClick(File directoryPath, Label title) {
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                FileItem selectedItem = table.getSelectionModel().getSelectedItem();
                selectedFile = new File(directoryPath + File.separator + selectedItem.getName());
                currentDirectoryPath = selectedFile.isDirectory() ? selectedFile.getAbsolutePath() : selectedFile.getParent();

                if (!selectedFile.isDirectory()) {
                    try {
                        displayFileInfo(selectedFile, title);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        displayFiles(currentDirectoryPath, title);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * Displays information about the selected file.
     *
     * @param file  the selected file
     * @param title the label to display file information
     * @throws IOException if an I/O error occurs
     */
    protected void displayFileInfo(File file, Label title) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String fileInfo = "Name: " + file.getName() + ";   " + "Size: " + file.length() + " bytes;   " + "Owner: " + owner(file.getAbsolutePath()) + ";   " + "Created: " + sdf.format(new Date(file.lastModified()));

        title.setText(fileInfo);
    }

    /**
     * Retrieves the owner of a file.
     *
     * @param dir the path to the file or directory
     * @return the owner of the file
     */
    protected String owner(String dir) {
        try {
            Path path = Paths.get(dir);
            FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            UserPrincipal owner = ownerAttributeView.getOwner();
            int len = owner.getName().length();
            int last_sep = owner.getName().lastIndexOf(File.separator);
            return owner.getName().substring(last_sep + 1, len);
        } catch (IOException | SecurityException e) {
            logger.error("Unable to determine the file owner");
            return "-";
        }
    }

    /**
     * Calculates the string representation of a file size in bytes, kilobytes, or megabytes.
     *
     * @param size the file size
     * @return the string representation of the file size with the unit of measurement
     */
    protected String calcResSize(double size) {
        double size_doub;
        if (size < 1000) {
            size_res = (int) size + " bytes";
        } else if (1000 <= size && size < 100000) {
            size_doub = size / (Math.pow(2, 10));
            size_res = String.format("%.1f", size_doub) + " Kb";
        } else {
            size_doub = size / (Math.pow(2, 20));
            size_res = String.format("%.1f", size_doub) + " Mb";
        }
        return size_res;
    }

    /**
     * Recursively calculates the size of a folder.
     *
     * @param path the path to the folder
     * @return the size of the folder in bytes
     */
    protected double calculateFolderSize(String path) {
        File folder = new File(path);
        long size = 0;
        if (folder.isFile()) {
            size += folder.length();
        } else {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += calculateFolderSize(file.getAbsolutePath());
                    }
                }
            }
        }
        return size;
    }

    /**
     * Calculates the folder size and displays information about files and folders.
     *
     * @param directoryPath the path to the directory
     * @param title         the label to display information
     */
    protected void calcSizeFunc(String directoryPath, Label title) {
        if (directoryPath.isEmpty()) {
            title.setText("Directory is empty");
        } else {
            table.getItems().clear();
            title.setText("Calculating folder size...");

            CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                File directory = new File(directoryPath);
                int count = 0;
                for (File file : directory.listFiles()) {
                    if (file.isDirectory()) {
                        count++;
                        final int finalCount = count;
                        Platform.runLater(() -> title.setText("Already calculated: " + finalCount + " folders"));
                    }
                    String name = file.getName();
                    String type = file.isDirectory() ? "Folder" : "File";
                    String size = file.isDirectory() ? calcResSize(calculateFolderSize(file.getAbsolutePath())) : calcResSize(file.length());
                    String owner = owner(file.getAbsolutePath());
                    String date = getFormattedDate(file.lastModified());
                    table.getItems().add(new FileItem(name, type, size, owner, date));
                }
                long endTime = System.currentTimeMillis();
                time = endTime - startTime;
                return count;
            }).thenAcceptAsync(count -> {
                Platform.runLater(() -> title.setText("Total: calculated " + count + " folders in " + time + " ms"));
            });
        }
    }

    private int i = 3;
    private int count_of_back = 0;

    /**
     * Navigates back through directories or folders.
     *
     * @param title the label to display information
     * @throws IOException if an I/O error occurs
     */
    protected void goBack(Label title) throws IOException {
        if (currentDirectoryPath.isEmpty()) {
            title.setText("Directory is empty");
        } else {
            title.setText("");
            int lastIndex = currentDirectoryPath.lastIndexOf(File.separator);
            if (lastIndex <= 2) {
                if (!inDisk) {
                    currentDirectoryPath = currentDirectoryPath.substring(0, lastIndex + 1);
                    displayFiles(currentDirectoryPath, title);
                    inDisk = true;
                } else {
                    count_of_back++;
                    if (count_of_back >= 5) {
                        if (i > 0) {
                            title.setText(i + " times left");
                            i--;
                        } else {
                            title.setText("You're so good in pushing:)");
                        }

                    } else {
                        title.setText("Too much Back");
                    }
                }
            } else {
                currentDirectoryPath = currentDirectoryPath.substring(0, lastIndex);
                displayFiles(currentDirectoryPath, title);
            }
        }
    }

    /**
     * Formats the date into a string representation.
     *
     * @param millis milliseconds representing the date
     * @return formatted date string
     */
    protected String getFormattedDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date(millis));
    }
}
