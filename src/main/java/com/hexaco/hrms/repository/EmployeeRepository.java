package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNicNumber(String nicNumber);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByPhoneNumber(String phoneNumber);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEpfNumber(String epfNumber);

    Optional<Employee> findByEtfNumber(String etfNumber);

    Optional<Employee> findByFingerprintUserId(Long fingerprintUserId);

    List<Employee> findByDepartmentIgnoreCase(String department);

    List<Employee> findByReportingOfficerId(Long reportingOfficerId);

    List<Employee> findByBranch(String branch);

    long countByDepartment(String department);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT e.department, COUNT(e) FROM Employee e GROUP BY e.department")
    List<Object[]> countEmployeesByDepartment();

    @Query("SELECT e.designation.designationName, COUNT(e) FROM Employee e GROUP BY e.designation.designationName")
    List<Object[]> countEmployeesByDesignation();

    @Query("SELECT e.employeeType, COUNT(e) FROM Employee e GROUP BY e.employeeType")
    List<Object[]> countEmployeesByType();

    @Query("SELECT e.branch, COUNT(e) FROM Employee e GROUP BY e.branch")
    List<Object[]> countEmployeesByBranch();

    @Query("SELECT DISTINCT e.branch FROM Employee e WHERE e.branch IS NOT NULL AND e.branch != ''")
    List<String> findAllDistinctBranches();
}
