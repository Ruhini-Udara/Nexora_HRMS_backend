package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.service.LeaveCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-calculation")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveCalculationController {

    private final LeaveCalculationService leaveCalculationService;

    public LeaveCalculationController(LeaveCalculationService leaveCalculationService) {
        this.leaveCalculationService = leaveCalculationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, String>> calculateLeave(@RequestParam int year) {
        leaveCalculationService.calculateLeaveForYear(year);
        return ResponseEntity.ok(Map.of("message", "Leave calculation completed successfully for year " + year));
    }

    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalance>> getLeaveBalances(
            @RequestParam int year,
            @RequestParam(required = false) String branch) {
        List<LeaveBalance> balances = leaveCalculationService.getLeaveBalancesByBranchAndYear(branch, year);
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/finalize")
    public ResponseEntity<Map<String, String>> finalizeLeave(
            @RequestParam int year,
            @RequestParam(required = false) String branch,
            @RequestParam Long finalizedById) {
        leaveCalculationService.finalizeLeaveBalancesForBranch(branch, year, finalizedById);
        return ResponseEntity.ok(Map.of("message", "Leave balances finalized successfully"));
    }

    @PutMapping("/adjust")
    public ResponseEntity<LeaveBalance> adjustLeave(
            @RequestParam Long balanceId,
            @RequestParam int annual,
            @RequestParam int casual,
            @RequestParam int medical,
            @RequestParam Long editedById) {
        LeaveBalance adjusted = leaveCalculationService.manuallyAdjustLeaveBalance(balanceId, annual, casual, medical, editedById);
        return ResponseEntity.ok(adjusted);
    }
}
