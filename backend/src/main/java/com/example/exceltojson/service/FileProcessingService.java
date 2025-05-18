package com.example.exceltojson.service;

import com.example.exceltojson.model.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class FileProcessingService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String lastProcessedJson;

    public String processFiles(MultipartFile excelFile, MultipartFile jsonFile) throws IOException {
        // Read JSON file and find the first array key
        JsonNode rootNode = objectMapper.readTree(jsonFile.getInputStream());
        String arrayKey = findFirstArrayKey(rootNode);
        if (arrayKey == null) {
            throw new IllegalArgumentException("JSON file must contain at least one array of objects");
        }

        // Get the array node and its first object to determine expected fields
        JsonNode arrayNode = rootNode.get(arrayKey);
        if (!arrayNode.isArray() || arrayNode.size() == 0) {
            throw new IllegalArgumentException("JSON array must contain at least one object");
        }

        // Get expected fields from the first object in the array
        JsonNode firstObject = arrayNode.get(0);
        Set<String> expectedFields = new HashSet<>();
        firstObject.fields().forEachRemaining(entry -> expectedFields.add(entry.getKey()));

        // Read Excel file
        try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file must contain a header row");
            }

            // Create mapping of column indices to names
            Map<Integer, String> columnMapping = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String columnName = cell.getStringCellValue().trim();
                    if (!columnName.isEmpty()) {
                        columnMapping.put(i, columnName);
                    }
                }
            }

            // Log missing fields instead of throwing an error
            Set<String> missingFields = new HashSet<>(expectedFields);
            missingFields.removeAll(columnMapping.values());
            if (!missingFields.isEmpty()) {
                System.out.println("Note: The following columns from JSON are not present in Excel: " + String.join(", ", missingFields));
            }

            // Process each row
            List<Map<String, String>> items = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> itemData = new HashMap<>();
                boolean hasData = false;
                
                for (Map.Entry<Integer, String> entry : columnMapping.entrySet()) {
                    int columnIndex = entry.getKey();
                    String columnName = entry.getValue();
                    Cell cell = row.getCell(columnIndex);
                    
                    if (cell != null) {
                        String value = getCellValueAsString(cell);
                        if (value != null && !value.isEmpty()) {
                            itemData.put(columnName, value);
                            hasData = true;
                        }
                    }
                }
                
                if (hasData) {
                    items.add(itemData);
                }
            }

            // Create new JSON structure
            Map<String, Object> newJsonData = new HashMap<>();
            newJsonData.put(arrayKey, items);
            
            // Store and return the updated JSON
            lastProcessedJson = objectMapper.writeValueAsString(newJsonData);
            return lastProcessedJson;
        }
    }

    private String findFirstArrayKey(JsonNode rootNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (entry.getValue().isArray() && entry.getValue().size() > 0 && entry.getValue().get(0).isObject()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public byte[] getLatestJsonData() throws IOException {
        if (lastProcessedJson == null) {
            // If no data has been processed yet, return an empty JSON structure
            Map<String, List<Map<String, String>>> emptyData = new HashMap<>();
            emptyData.put("data", new ArrayList<>());
            return objectMapper.writeValueAsString(emptyData).getBytes();
        }
        return lastProcessedJson.getBytes();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Format numeric values without scientific notation
                return String.format("%.0f", cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }
} 