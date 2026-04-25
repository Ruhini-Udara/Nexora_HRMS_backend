package com.hexaco.hrms.dto;

import lombok.Data;

@Data
public class EmployeeDTO {
    // Step 1: Personal Info
    private String nicNumber;
    private String sex;
    private String fullName;
    private String surname;
    private String dateOfBirth;
    private String dateJoined;
    private String email;
    private String homeAddress;
    private String maritalStatus;

    // Step 2: Employment Info
    private Long designationId;
    private String employeeType;
    private String department;
    private String epfNumber;
    private String etfNumber;

    // Step 3: System Access
    private String accountEmail; // The role-based email (e.g. director@nexora.com)
    private String password;
    private String roleName;
    private boolean enableSystemAccess;
}
