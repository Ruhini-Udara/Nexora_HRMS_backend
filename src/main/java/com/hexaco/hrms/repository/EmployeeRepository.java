package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNicNumber(String nicNumber);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByFingerprintUserId(Long fingerprintUserId);
    java.util.List<Employee> findByDepartmentIgnoreCase(String department);
}
