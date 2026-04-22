package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.Document;
import com.hexaco.hrms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        Document savedDocument = documentService.saveDocument(document);
        return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocumentsByRef(
            @RequestParam Long refId, 
            @RequestParam String refType) {
        List<Document> documents = documentService.getDocumentsByRef(refId, refType);
        return ResponseEntity.ok(documents);
    }
}
