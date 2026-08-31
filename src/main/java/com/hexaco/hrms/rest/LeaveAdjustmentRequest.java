package com.hexaco.hrms.rest;

import lombok.Data;

@Data
public class LeaveAdjustmentRequest {
    private Long balanceId;
    private String leaveType; // ANNUAL, CASUAL, MEDICAL
    private Integer newBalance;
    private String reason;
    private Long adjustedById;
}
