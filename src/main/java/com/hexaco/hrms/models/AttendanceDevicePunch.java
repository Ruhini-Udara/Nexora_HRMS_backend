package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_device_punch",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_device_punch_device_source_record",
                        columnNames = {"attendance_device_id", "source_record_key"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDevicePunch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attendance_device_id", nullable = false)
    private AttendanceDevice attendanceDevice;

    @Column(name = "terminal_user_id", nullable = false)
    private Long terminalUserId;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Column(name = "source_record_key", nullable = false)
    private String sourceRecordKey;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean processed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (processed == null) {
            processed = false;
        }
    }
}
