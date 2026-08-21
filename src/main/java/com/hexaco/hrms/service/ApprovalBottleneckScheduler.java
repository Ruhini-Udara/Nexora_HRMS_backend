package com.hexaco.hrms.service;

import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.MaternityLeaveRepository;
import com.hexaco.hrms.repository.OverseasLeaveRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalBottleneckScheduler {

    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;
    private final UserAccountRepository userAccountRepository;
    private final SystemNotificationService notificationService;

    // Run once a day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void trackApprovalBottlenecks() {
        log.info("Starting approval bottleneck tracking...");
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(2);

        // Find pending Overseas Leaves
        List<OverseasLeave> delayedOverseasLeaves = overseasLeaveRepository.findByStatusAndCreatedAtBefore("SUBMITTED", thresholdDate);
        for (OverseasLeave leave : delayedOverseasLeaves) {
            notifyApprovers("Overseas Leave Pending Approval", 
                            "Overseas leave for " + leave.getEmployee().getFullName() + " has been pending for over 2 days.",
                            "/hr/leave-requests/overseas-leave");
        }

        // Find pending Maternity Leaves
        List<MaternityLeave> delayedMaternityLeaves = maternityLeaveRepository.findByStatusAndCreatedAtBefore("SUBMITTED", thresholdDate);
        for (MaternityLeave leave : delayedMaternityLeaves) {
            notifyApprovers("Maternity Leave Pending Approval", 
                            "Maternity leave for " + leave.getEmployee().getFullName() + " has been pending for over 2 days.",
                            "/hr/leave-requests/maternity-leave");
        }
        
        log.info("Finished approval bottleneck tracking.");
    }

    private void notifyApprovers(String title, String message, String link) {
        // Find HR role users
        List<UserAccount> hrUsers = userAccountRepository.findByRoleRoleName("ROLE_HR");
        for (UserAccount hr : hrUsers) {
            notificationService.createNotification(hr.getEmployee(), title, message, link);
        }
    }
}
