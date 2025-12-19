package edu.univ.erp.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileUtil {

    // Read file content as string
    public static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            return null;
        }
    }

    // Write string to file
    public static boolean writeFile(String filePath, String content) {
        try (PrintWriter out = new PrintWriter(filePath)) {
            out.println(content);
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + filePath);
            return false;
        }
    }

    // Read file as list of lines
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        }
        return lines;
    }

    // Check if file exists
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // Create directory if it doesn't exist
    public static boolean createDirectory(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
            return true;
        } catch (IOException e) {
            System.err.println("Error creating directory: " + dirPath);
            return false;
        }
    }

    // Get file extension
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    // =========================================================================
    // NEW METHODS ADDED FOR SYSTEM SETTINGS PANEL
    // =========================================================================

    /**
     * Open file chooser for SQL backup files
     */
    public static File chooseBackupFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Backup File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQL Backup Files", "sql"));

        // Set current directory to backups if it exists
        File backupDir = new File("backups");
        if (backupDir.exists()) {
            fileChooser.setCurrentDirectory(backupDir);
        }

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Format file size for display
     */
    public static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        else return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Format date for display
     */
    public static String formatDate(Date date) {
        if (date == null) return "Unknown";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * Check if file exists and is readable
     */
    public static boolean isFileValid(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    /**
     * Get list of files in a directory with specific extension
     */
    public static List<File> getFilesByExtension(String directoryPath, String extension) {
        List<File> files = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] allFiles = directory.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (file.isFile() && getFileExtension(file.getName()).equalsIgnoreCase(extension)) {
                        files.add(file);
                    }
                }
            }
        }

        return files;
    }

    /**
     * Delete a file safely
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.delete();
        } catch (Exception e) {
            System.err.println("Error deleting file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Copy file from source to destination
     */
    public static boolean copyFile(String sourcePath, String destPath) {
        try (InputStream in = new FileInputStream(sourcePath);
             OutputStream out = new FileOutputStream(destPath)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get file creation date
     */
    public static Date getFileCreationDate(File file) {
        try {
            return new Date(file.lastModified());
        } catch (Exception e) {
            System.err.println("Error getting file creation date: " + e.getMessage());
            return new Date();
        }
    }

    /**
     * Check if directory is writable
     */
    public static boolean isDirectoryWritable(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return dir.canWrite();
    }

    /**
     * Get available disk space in a directory
     */
    public static long getAvailableDiskSpace(String path) {
        File file = new File(path);
        return file.getFreeSpace();
    }

    /**
     * Create a temporary file with content
     */
    public static File createTempFile(String content, String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(content);
            }
            return tempFile;
        } catch (IOException e) {
            System.err.println("Error creating temp file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Read file and return as byte array
     */
    public static byte[] readFileToByteArray(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Error reading file to byte array: " + filePath);
            return null;
        }
    }

    /**
     * Write byte array to file
     */
    public static boolean writeByteArrayToFile(String filePath, byte[] data) {
        try {
            Files.write(Paths.get(filePath), data);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing byte array to file: " + filePath);
            return false;
        }
    }
}