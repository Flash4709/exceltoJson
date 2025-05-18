package com.example.exceltojson.controller;

import com.example.exceltojson.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileProcessingService fileProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("excelFile") MultipartFile excelFile,
            @RequestParam("jsonFile") MultipartFile jsonFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file types
            if (!excelFile.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                response.put("success", false);
                response.put("message", "Invalid Excel file format. Please upload an .xlsx file.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!jsonFile.getContentType().equals("application/json")) {
                response.put("success", false);
                response.put("message", "Invalid JSON file format. Please upload a .json file.");
                return ResponseEntity.badRequest().body(response);
            }

            // Process files
            fileProcessingService.processFiles(excelFile, jsonFile);
            
            response.put("success", true);
            response.put("message", "Files processed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing files: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFile() {
        try {
            byte[] data = fileProcessingService.getUpdatedJsonFile();
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=updated_stores.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(data.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 