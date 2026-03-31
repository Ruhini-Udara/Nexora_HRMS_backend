package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_id", nullable = false)
    private Long refId; // This stores the ID of the entity (e.g., OverseasLeave ID)

    @Column(name = "ref_type", nullable = false)
    private String refType; // This tells us which table the ID belongs to ('OVERSEAS_LEAVE', 'MATERNITY_LEAVE')

    @Column(name = "document_type")
    private String documentType; // e.g., 'PASSPORT_COPY', 'MEDICAL_CERT'

    @Column(name = "file_path_url", nullable = false)
    private String filePathUrl;

    private String description;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        uploadedAt = LocalDateTime.now();
    }
}
