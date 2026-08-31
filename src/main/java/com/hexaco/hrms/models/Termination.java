package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "termination")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Termination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "epf_number")
    private String epfNumber;

    @Column(name = "branch")
    private String branch;

    @Column(name = "type")
    private String type;

    @Column(name = "initiation_date")
    private LocalDate initiationDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "special_remark", columnDefinition = "TEXT")
    private String specialRemark;

    @Column(nullable = false)
    private String status;

    @Column(name = "request_for_termination_doc")
    private String requestForTerminationDoc;

    @Column(name = "loan_clearance_letter_doc")
    private String loanClearanceLetterDoc;

    @Column(name = "other_document_doc")
    private String otherDocumentDoc;

    @Column(name = "hr_remark", columnDefinition = "TEXT")
    private String hrRemark;

    @Column(name = "director_remark", columnDefinition = "TEXT")
    private String directorRemark;

    @Column(name = "board_meeting_date")
    private String boardMeetingDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
