package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "carry_forward_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarryForwardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnore
    private CarryForwardBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "location")
    private String location; // Grouping field

    @Column(name = "carried_forward_days", nullable = false)
    private Integer carriedForwardDays;

    @Column(name = "remarks")
    private String remarks;
}
