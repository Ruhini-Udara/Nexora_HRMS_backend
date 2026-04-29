package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "manual_attendance",
       uniqueConstraints = @UniqueConstraint(name = "uq_attendance", columnNames = {"employee_id", "attendance_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AttendanceShift shift;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    // PRESENT, ABSENT, LATE, HALF_DAY
    @Column(nullable = false)
    private String status;

    @Column(name = "in_time")
    private LocalTime inTime;

    @Column(name = "out_time")
    private LocalTime outTime;

    @Column(name = "work_hours", precision = 5, scale = 2)
    private BigDecimal workHours;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // The supervisor who submitted this record
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitted_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee submittedBy;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
