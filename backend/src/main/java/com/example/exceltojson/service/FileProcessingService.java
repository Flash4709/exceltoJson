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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final ObjectMapper objectMapper;
    private JsonFile currentJsonFile;

    public void processFiles(MultipartFile excelFile, MultipartFile jsonFile) throws IOException {
        // Read and parse JSON file
        JsonFile jsonData = objectMapper.readValue(jsonFile.getInputStream(), JsonFile.class);
        
        // Read Excel file
        List<Store> stores = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            // Get column indices
            int templateIndex = getColumnIndex(headerRow, "template");
            int nameIndex = getColumnIndex(headerRow, "name");
            int addressIndex = getColumnIndex(headerRow, "address");
            int logoIndex = getColumnIndex(headerRow, "logo");
            int emailIndex = getColumnIndex(headerRow, "email");

            // Process each row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Store store = new Store();
                store.setTemplate(getCellValueAsString(row.getCell(templateIndex)));
                store.setName(getCellValueAsString(row.getCell(nameIndex)));
                store.setAddress(getCellValueAsString(row.getCell(addressIndex)));
                store.setLogo(getCellValueAsString(row.getCell(logoIndex)));
                store.setEmail(getCellValueAsString(row.getCell(emailIndex)));

                stores.add(store);
            }
        }

        // Update JSON data
        jsonData.setStores(stores);
        currentJsonFile = jsonData;
    }

    public byte[] getUpdatedJsonFile() throws IOException {
        if (currentJsonFile == null) {
            throw new IllegalStateException("No processed JSON file available");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, currentJsonFile);
        return outputStream.toByteArray();
    }

    private int getColumnIndex(Row headerRow, String columnName) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && columnName.equalsIgnoreCase(cell.getStringCellValue())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
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
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
} 