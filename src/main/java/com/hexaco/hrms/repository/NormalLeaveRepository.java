package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.NormalLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NormalLeaveRepository extends JpaRepository<NormalLeave, Long> {
    List<NormalLeave> findByStatus(String status);
    List<NormalLeave> findByEmployeeId(Long employeeId);

    @Query("SELECT COUNT(l) FROM NormalLeave l WHERE l.employee.department = :department " +
           "AND l.status = 'APPROVED' " +
           "AND l.id != :excludeLeaveId " +
           "AND l.fromDate <= :endDate AND l.endDate >= :fromDate")
    long countOverlappingLeavesByDepartment(
            @Param("department") String department,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("excludeLeaveId") Long excludeLeaveId);
}
