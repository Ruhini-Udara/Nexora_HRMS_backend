package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByRefIdAndRefType(Long refId, String refType);
}
