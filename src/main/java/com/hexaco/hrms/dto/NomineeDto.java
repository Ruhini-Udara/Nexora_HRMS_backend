package com.hexaco.hrms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomineeDto {
    private Long employeeId;
    private String nomineeName;
    private String relationship;
    private String nic;
    private String phoneNo;
    private String address;
    private String bankName;
    private String bankBranch;
    private String accountNumber;
}
