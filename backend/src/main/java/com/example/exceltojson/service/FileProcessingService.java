package com.example.exceltojson.service;

import com.example.exceltojson.model.JsonFile;
import com.example.exceltojson.model.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final ObjectMapper objectMapper;
    private JsonFile currentJsonFile;

    public void processFiles(MultipartFile excelFile, MultipartFile jsonFile) throws IOException {
        // Read and parse JSON file
        JsonFile jsonData = objectMapper.readValue(jsonFile.getInputStream(), JsonFile.class);
        
        if (jsonData.getStores() == null || jsonData.getStores().isEmpty()) {
            throw new IllegalArgumentException("JSON file must contain at least one store entry");
        }

        // Get expected column names from the first store in JSON
        Store firstStore = jsonData.getStores().get(0);
        Set<String> expectedColumns = new HashSet<>();
        if (firstStore.getTemplate() != null) expectedColumns.add("template");
        if (firstStore.getAddress() != null) expectedColumns.add("address");
        if (firstStore.getName() != null) expectedColumns.add("name");
        if (firstStore.getLogo() != null) expectedColumns.add("logo");
        if (firstStore.getEmail() != null) expectedColumns.add("email");
        
        // Read Excel file
        List<Store> stores = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file must have a header row");
            }

            // Create column mapping and validate column names
            Map<Integer, String> columnMapping = new HashMap<>();
            Set<String> excelColumns = new HashSet<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String columnName = cell.getStringCellValue().trim().toLowerCase();
                    if (!columnName.isEmpty()) {
                        columnMapping.put(i, columnName);
                        excelColumns.add(columnName);
                    }
                }
            }

            if (columnMapping.isEmpty()) {
                throw new IllegalArgumentException("No valid columns found in Excel file");
            }

            // Validate column names
            validateColumnNames(excelColumns, expectedColumns);

            // Process each row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Store store = new Store();
                
                // Map each column to its corresponding field
                for (Map.Entry<Integer, String> entry : columnMapping.entrySet()) {
                    int columnIndex = entry.getKey();
                    String columnName = entry.getValue();
                    String value = getCellValueAsString(row.getCell(columnIndex));
                    
                    switch (columnName) {
                        case "template":
                            store.setTemplate(value);
                            break;
                        case "address":
                            store.setAddress(value);
                            break;
                        case "name":
                            store.setName(value);
                            break;
                        case "logo":
                            store.setLogo(value);
                            break;
                        case "email":
                            store.setEmail(value);
                            break;
                    }
                }

                stores.add(store);
            }
        }

        // Update JSON data
        jsonData.setStores(stores);
        currentJsonFile = jsonData;
    }

    private void validateColumnNames(Set<String> excelColumns, Set<String> expectedColumns) {
        // Convert both sets to lowercase for case-insensitive comparison
        Set<String> excelColumnsLower = excelColumns.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        Set<String> expectedColumnsLower = expectedColumns.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // Only check for missing columns
        Set<String> missingColumns = expectedColumnsLower.stream()
            .filter(col -> !excelColumnsLower.contains(col))
            .collect(Collectors.toSet());

        if (!missingColumns.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Column name mismatch detected:\n");
            
            // Convert back to original case for error message
            Set<String> missingOriginalCase = expectedColumns.stream()
                .filter(col -> missingColumns.contains(col.toLowerCase()))
                .collect(Collectors.toSet());
            errorMessage.append("Missing columns in Excel file: ")
                      .append(String.join(", ", missingOriginalCase))
                      .append("\n");
            
            errorMessage.append("\nPlease ensure all required columns are present in the Excel file.");
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

    public byte[] getUpdatedJsonFile() throws IOException {
        if (currentJsonFile == null) {
            throw new IllegalStateException("No processed JSON file available");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, currentJsonFile);
        return outputStream.toByteArray();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Remove trailing zeros for numeric values
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    return String.format("%.0f", value);
                }
                return String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
} 