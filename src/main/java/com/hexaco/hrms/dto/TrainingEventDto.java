package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEventDto {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    private Long id;
    private String title;
    private String category;
    private Integer expectedParticipants;
    private String description;
    private LocalDate proposedStartDate;
    private String time;
    private LocalDate applyBefore;
    private String location;
    private Double budget;
    private String instructor;
    private String status;
    private String approvedBy;
    private String approvedAt;
    private String reason;
}
