package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.OverseasLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverseasLeaveRepository extends JpaRepository<OverseasLeave, Long> {
    List<OverseasLeave> findByStatus(String status);
    List<OverseasLeave> findByEmployeeId(Long employeeId);
    List<OverseasLeave> findByStatusIgnoreCase(String status);
    List<OverseasLeave> findByStatusAndCreatedAtBefore(String status, java.time.LocalDateTime date);

    @Query("SELECT COUNT(l) FROM OverseasLeave l WHERE l.status = 'PENDING_HR_APPROVAL' OR l.status = 'PENDING_ADMIN_APPROVAL' OR l.status = 'SUBMITTED'")
    long countPending();

    @Query("SELECT l FROM OverseasLeave l WHERE l.passportExpDate BETWEEN :startDate AND :endDate AND (l.status = 'APPROVED' OR l.status = 'ADMIN_APPROVED')")
    List<OverseasLeave> findUpcomingPassportExpiries(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
    
    @Query("SELECT l.employee.department, COUNT(l) FROM OverseasLeave l WHERE l.status = 'APPROVED' OR l.status = 'ADMIN_APPROVED' GROUP BY l.employee.department")
    List<Object[]> countApprovedLeavesByDepartment();

    @Query("SELECT COUNT(l) FROM OverseasLeave l WHERE l.employee.department = :department AND (l.status = 'APPROVED' OR l.status = 'ADMIN_APPROVED') AND (l.fromDate <= :endDate AND l.endDate >= :fromDate) AND l.id != :excludeLeaveId")
    long countOverlappingLeavesByDepartment(@Param("department") String department, @Param("fromDate") java.time.LocalDate fromDate, @Param("endDate") java.time.LocalDate endDate, @Param("excludeLeaveId") Long excludeLeaveId);
}
