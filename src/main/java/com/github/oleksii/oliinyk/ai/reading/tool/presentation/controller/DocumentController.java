package com.github.oleksii.oliinyk.ai.reading.tool.presentation.controller;
import com.github.oleksii.oliinyk.ai.reading.tool.application.DocumentService;
import com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto.DocumentResponse;
import com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto.FolderDocumentsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ObjectKey is required");
        }

        DocumentService.DocumentDownload response = documentService.download(objectKey);

        String safeFileName = UriUtils.encode(response.documentName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + safeFileName)
                .header(HttpHeaders.CONTENT_TYPE, response.contentType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(response.sizeBytes()))
                .body(response.resource());
    }

    @GetMapping("/files")
    public ResponseEntity<List<FolderDocumentsResponse>> getListOfFiles(){
        return ResponseEntity.ok(documentService.getListOfFiles());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DocumentResponse> delete(@RequestParam String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ObjectKey is required");
        }

        return ResponseEntity.ok(documentService.delete(objectKey));
    }
}

