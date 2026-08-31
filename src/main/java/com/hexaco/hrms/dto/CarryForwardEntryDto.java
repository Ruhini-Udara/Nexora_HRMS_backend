package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarryForwardEntryDto {
    private String empId;
    private String name;
    private String department;
    private Integer carriedForwardDays;
    private String remarks;
}
