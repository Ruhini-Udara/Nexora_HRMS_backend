package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'APPROVED' AND " +
           "(MONTH(l.fromDate) = :month AND YEAR(l.fromDate) = :year OR " +
           "MONTH(l.endDate) = :month AND YEAR(l.endDate) = :year)")
    List<LeaveRequest> findApprovedLeavesByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
