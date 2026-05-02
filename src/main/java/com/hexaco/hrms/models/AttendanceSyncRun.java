package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_sync_run",
        indexes = @Index(
                name = "idx_attendance_sync_run_started_at",
                columnList = "started_at"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSyncRun {

    public enum Status {
        IN_PROGRESS,
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_device_id")
    private AttendanceDevice attendanceDevice;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "received_count", nullable = false)
    @Builder.Default
    private Integer receivedCount = 0;

    @Column(name = "inserted_count", nullable = false)
    @Builder.Default
    private Integer insertedCount = 0;

    @Column(name = "duplicate_count", nullable = false)
    @Builder.Default
    private Integer duplicateCount = 0;

    @Column(name = "failed_count", nullable = false)
    @Builder.Default
    private Integer failedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String message;
}
