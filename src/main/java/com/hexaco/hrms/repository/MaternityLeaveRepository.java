package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.MaternityLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaternityLeaveRepository extends JpaRepository<MaternityLeave, Long> {
    List<MaternityLeave> findByStatus(String status);
    List<MaternityLeave> findByEmployeeId(Long employeeId);
    List<MaternityLeave> findByStatusAndCreatedAtBefore(String status, java.time.LocalDateTime date);

    @Query("SELECT COUNT(l) FROM MaternityLeave l WHERE l.status = 'PENDING_HR_APPROVAL' OR l.status = 'PENDING_ADMIN_APPROVAL' OR l.status = 'SUBMITTED'")
    long countPending();

    @Query("SELECT l FROM MaternityLeave l WHERE l.endDate BETWEEN :startDate AND :endDate AND (l.status = 'APPROVED' OR l.status = 'ADMIN_APPROVED')")
    List<MaternityLeave> findUpcomingMaternityReturns(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
    
    @Query("SELECT l.employee.department, COUNT(l) FROM MaternityLeave l WHERE l.status = 'APPROVED' OR l.status = 'ADMIN_APPROVED' GROUP BY l.employee.department")
    List<Object[]> countApprovedLeavesByDepartment();

    @Query("SELECT COUNT(l) FROM MaternityLeave l WHERE l.employee.department = :department AND (l.status = 'APPROVED' OR l.status = 'ADMIN_APPROVED') AND (l.fromDate <= :endDate AND l.endDate >= :fromDate) AND l.id != :excludeLeaveId")
    long countOverlappingLeavesByDepartment(@Param("department") String department, @Param("fromDate") java.time.LocalDate fromDate, @Param("endDate") java.time.LocalDate endDate, @Param("excludeLeaveId") Long excludeLeaveId);
}
