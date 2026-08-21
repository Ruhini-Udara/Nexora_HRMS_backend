package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "normal_leave")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalLeave extends LeaveRequest {

    private String branch;

    @Column(name = "contact_number")
    private String contactNumber;
}
