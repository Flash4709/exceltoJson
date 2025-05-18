package com.example.exceltojson.controller;

import com.example.exceltojson.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    private final FileProcessingService fileProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestParam("excelFile") MultipartFile excelFile,
            @RequestParam("jsonFile") MultipartFile jsonFile) {
        
        log.info("Received files - Excel: {}, JSON: {}", excelFile.getOriginalFilename(), jsonFile.getOriginalFilename());
        
        try {
            String updatedJson = fileProcessingService.processFiles(excelFile, jsonFile);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedJson);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing files", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error processing files: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFile() {
        try {
            byte[] data = fileProcessingService.getLatestJsonData();
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=updated_stores.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(data.length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading file", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 