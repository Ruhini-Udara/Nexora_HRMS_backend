package com.hexaco.hrms.dto;

import lombok.Data;

@Data
public class EmployeeUpdateDTO {
    private String fullName;
    private String email;
    private String department;
    private String branch;
    private Long designationId;
    private String employeeType;
}
