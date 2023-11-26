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
 * Класс с основными функциями для работы с файлами и отображения данных в приложении.
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
     * Выбор диска или директории.
     *
     * @param primaryStage основная сцена приложения
     * @param title        метка для отображения пути к выбранной директории
     * @throws IOException если происходит ошибка ввода-вывода
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
     * Отображение файлов в указанной директории.
     *
     * @param DirectoryPath путь к директории
     * @param title         метка для отображения пути к директории
     * @throws IOException если происходит ошибка ввода-вывода
     */
    protected void displayFiles(String DirectoryPath, Label title) throws IOException {
        table.getItems().clear();
        File directory = new File(DirectoryPath);
        title.setText("'" + DirectoryPath + "'");

        for (File file : directory.listFiles()) {
            double size_file;
            long file_length = file.length();
            if (file_length < 1000) {
                size_file = file_length;
                size_res = (int) size_file + " bytes";
            } else if (1000 <= file_length && file_length < 100000) {
                size_file = file_length / (Math.pow(2, 10));
                size_res = String.format("%.1f", size_file) + " Kb";
            } else {
                size_file = file_length / (Math.pow(2, 20));
                size_res = String.format("%.1f", size_file) + " Mb";
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
     * Обработчик щелчка мыши для отображения информации о файле или перехода к новой директории.
     *
     * @param DirectoryPath путь к директории
     * @param title         метка для отображения информации о файле или директории
     */
    protected void mouseClick(File DirectoryPath, Label title) {
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                FileItem selectedItem = table.getSelectionModel().getSelectedItem();
                selectedFile = new File(DirectoryPath + File.separator + selectedItem.getName());
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
     * Отображение информации о выбранном файле.
     *
     * @param file  выбранный файл
     * @param title метка для отображения информации о файле
     * @throws IOException если происходит ошибка ввода-вывода
     */
    protected void displayFileInfo(File file, Label title) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String fileInfo = "Name: " + file.getName() + ";   " + "Size: " + file.length() + " bytes;   " + "Owner: " + owner(file.getAbsolutePath()) + ";   " + "Created: " + sdf.format(new Date(file.lastModified()));

        title.setText(fileInfo);
    }

    /**
     * Получение владельца файла.
     *
     * @param dir путь к файлу или директории
     * @return владелец файла
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
            logger.error("Невозможно определить владельца файла");
            return "-";
        }
    }

    /**
     * Вычисляет строковое представление размера файла в байтах, килобайтах или мегабайтах.
     *
     * @param size размер файла
     * @return строковое представление размера файла с указанием единицы измерения
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
     * Рекурсивно вычисляет размер папки.
     *
     * @param path путь к папке
     * @return размер папки в байтах
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
     * Выполняет расчет размера папки и отображает информацию о файлах и папках.
     *
     * @param DirectoryPath путь к директории
     * @param title         метка для отображения информации
     */
    protected void calcSizeFunc(String DirectoryPath, Label title) {
        if (DirectoryPath.isEmpty()) {
            title.setText("Directory is empty");
        } else {
            table.getItems().clear();
            title.setText("Calculating folder size...");

            CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                File directory = new File(DirectoryPath);
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
     * Переходит назад по директориям или каталогам.
     *
     * @param title метка для отображения информации
     * @throws IOException если происходит ошибка ввода-вывода
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
     * Форматирует дату в строковое представление.
     *
     * @param millis миллисекунды, представляющие дату
     * @return отформатированная строка даты
     */
    protected String getFormattedDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date(millis));
    }
}

