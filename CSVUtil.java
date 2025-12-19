package edu.univ.erp.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class for CSV import/export operations
 * Handles reading and writing CSV files with proper formatting
 */
public class CSVUtil {

    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_QUOTE = "\"";

    /**
     * Export data to CSV file
     * @param filePath Path where CSV will be saved
     * @param headers Column headers
     * @param data 2D array of data (each row is an Object[])
     * @return true if export successful, false otherwise
     */
    public static boolean exportToCSV(String filePath, String[] headers, Object[][] data) {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            // Write headers
            StringBuilder headerLine = new StringBuilder();
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) headerLine.append(CSV_SEPARATOR);
                headerLine.append(escapeCSVField(headers[i]));
            }
            writer.println(headerLine.toString());

            // Write data rows
            for (Object[] row : data) {
                StringBuilder dataLine = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) dataLine.append(CSV_SEPARATOR);
                    dataLine.append(escapeCSVField(String.valueOf(row[i])));
                }
                writer.println(dataLine.toString());
            }

            writer.flush();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Import data from CSV file
     * @param filePath Path to CSV file
     * @return List of rows, each row is a List of String values
     */
    public static List<List<String>> importFromCSV(String filePath) {
        List<List<String>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> row = parseCSVLine(line);
                data.add(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Parse a single CSV line handling quoted fields
     * @param line CSV line to parse
     * @return List of field values
     */
    private static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Handle quote: either start/end quote or escaped quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote ""
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator (only outside quotes)
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // Add last field
        fields.add(current.toString().trim());

        return fields;
    }

    /**
     * Escape CSV field value for proper formatting
     * @param field Field value to escape
     * @return Properly escaped field
     */
    private static String escapeCSVField(String field) {
        if (field == null) return "";

        // If field contains comma, quote, or newline, wrap in quotes
        if (field.contains(CSV_SEPARATOR) || field.contains(CSV_QUOTE) || field.contains("\n")) {
            return CSV_QUOTE + field.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE) + CSV_QUOTE;
        }

        return field;
    }

    /**
     * Get specific column from imported CSV data
     * @param data Imported CSV data
     * @param columnIndex Column index (0-based)
     * @return List of values in that column (excluding header)
     */
    public static List<String> getColumn(List<List<String>> data, int columnIndex) {
        List<String> column = new ArrayList<>();

        if (data.isEmpty()) return column;

        // Skip header row (index 0)
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            if (columnIndex < row.size()) {
                column.add(row.get(columnIndex));
            }
        }

        return column;
    }

    /**
     * Get headers from imported CSV data
     * @param data Imported CSV data
     * @return List of header values
     */
    public static List<String> getHeaders(List<List<String>> data) {
        if (data.isEmpty()) return new ArrayList<>();
        return data.get(0);
    }

    /**
     * Get data rows (excluding header) from imported CSV
     * @param data Imported CSV data
     * @return List of data rows
     */
    public static List<List<String>> getDataRows(List<List<String>> data) {
        if (data.size() <= 1) return new ArrayList<>();
        return data.subList(1, data.size());
    }
}
