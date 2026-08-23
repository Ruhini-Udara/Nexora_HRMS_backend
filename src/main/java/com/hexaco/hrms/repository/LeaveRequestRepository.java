package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT lr.employee.id FROM LeaveRequest lr WHERE lr.fromDate <= :date AND lr.endDate >= :date AND lr.status = 'APPROVED'")
    List<Long> findApprovedLeaveEmployeeIdsByDate(@Param("date") LocalDate date);
}
