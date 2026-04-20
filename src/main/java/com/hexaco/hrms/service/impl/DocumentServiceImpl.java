package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.Document;
import com.hexaco.hrms.repository.DocumentRepository;
import com.hexaco.hrms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Override
    public Document saveDocument(Document document) {
        if (document.getUploadedAt() == null) {
            document.setUploadedAt(LocalDateTime.now());
        }
        return documentRepository.save(document);
    }

    @Override
    public List<Document> getDocumentsByRef(Long refId, String refType) {
        return documentRepository.findByRefIdAndRefType(refId, refType);
    }
}
