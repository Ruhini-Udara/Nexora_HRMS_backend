package com.hexaco.hrms.service;

import com.hexaco.hrms.models.Document;

import java.util.List;

public interface DocumentService {
    Document saveDocument(Document document);
    List<Document> getDocumentsByRef(Long refId, String refType);
}
