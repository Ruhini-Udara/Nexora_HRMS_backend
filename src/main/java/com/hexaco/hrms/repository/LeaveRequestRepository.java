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

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'APPROVED' AND " +
           "(MONTH(l.fromDate) = :month AND YEAR(l.fromDate) = :year OR " +
           "MONTH(l.endDate) = :month AND YEAR(l.endDate) = :year)")
    List<LeaveRequest> findApprovedLeavesByMonthAndYear(@Param("month") int month, @Param("year") int year);
    @Query("SELECT lr.employee.id FROM LeaveRequest lr WHERE lr.fromDate <= :date AND lr.endDate >= :date AND lr.status = 'APPROVED'")
    List<Long> findApprovedLeaveEmployeeIdsByDate(@Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND UPPER(lr.status) = 'APPROVED' AND lr.endDate >= :today ORDER BY lr.fromDate ASC")
    List<LeaveRequest> findUpcomingApprovedLeaves(@Param("employeeId") Long employeeId, @Param("today") LocalDate today);
}
