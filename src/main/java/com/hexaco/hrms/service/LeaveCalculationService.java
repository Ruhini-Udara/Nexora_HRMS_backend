package com.hexaco.hrms.service;

import com.hexaco.hrms.models.LeaveBalance;
import java.util.List;

public interface LeaveCalculationService {
    void calculateLeaveForYear(int year);
    List<LeaveBalance> getLeaveBalancesByYear(int year);
    List<LeaveBalance> getLeaveBalancesByBranchAndYear(String branch, int year);
    void finalizeLeaveBalancesForBranch(String branch, int year, Long finalizedById);
    LeaveBalance manuallyAdjustLeaveBalance(Long balanceId, int annual, int casual, int medical, Long editedById);
}
