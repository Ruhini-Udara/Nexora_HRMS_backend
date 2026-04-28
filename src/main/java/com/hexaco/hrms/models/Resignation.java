package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resignation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resignation {

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

    @Column(name = "designation")
    private String designation;

    @Column(name = "branch")
    private String branch;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "last_working_date")
    private LocalDate lastWorkingDate;

    @Column(name = "obligation_details", columnDefinition = "TEXT")
    private String obligationDetails;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "special_remark", columnDefinition = "TEXT")
    private String specialRemark;

    @Column(nullable = false)
    private String status;

    @Column(name = "resignation_letter_doc")
    private String resignationLetterDoc;

    @Column(name = "clearance_letter_doc")
    private String clearanceLetterDoc;

    @Column(name = "handover_checklist_doc")
    private String handoverChecklistDoc;

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
