package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "overseas_leave")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverseasLeave extends LeaveRequest {

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_exp_date")
    private LocalDate passportExpDate;

    private String branch;

    @Column(name = "contact_number")
    private String contactNumber;

    private String email;

    @Column(name = "special_remark", columnDefinition = "TEXT")
    private String specialRemark;

    // Document paths in Supabase Storage — signed URLs are generated on demand
    @Column(name = "leave_letter_path")
    private String leaveLetterPath;

    @Column(name = "passport_copy_path")
    private String passportCopyPath;

    @Column(name = "visa_copy_path")
    private String visaCopyPath;

    @Column(name = "confirmation_letter_path")
    private String confirmationLetterPath;

    @Column(name = "flight_tickets_path")
    private String flightTicketsPath;
}
