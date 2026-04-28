package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEvent {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getExpectedParticipants() { return expectedParticipants; }
    public void setExpectedParticipants(Integer expectedParticipants) { this.expectedParticipants = expectedParticipants; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getProposedStartDate() { return proposedStartDate; }
    public void setProposedStartDate(LocalDate proposedStartDate) { this.proposedStartDate = proposedStartDate; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public LocalDate getApplyBefore() { return applyBefore; }
    public void setApplyBefore(LocalDate applyBefore) { this.applyBefore = applyBefore; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category; // e.g., Soft Skills, Technical Training

    @Column(name = "expected_participants")
    private Integer expectedParticipants;

    @Column(length = 1000)
    private String description;

    @Column(name = "proposed_start_date")
    private LocalDate proposedStartDate;

    private String time;

    @Column(name = "apply_before")
    private LocalDate applyBefore;

    private String location;

    private Double budget;

    private String instructor;

    @Column(nullable = false)
    private String status; // e.g., Published, Draft

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "Published"; // Defaulting based on frontend behavior
        }
    }
}
