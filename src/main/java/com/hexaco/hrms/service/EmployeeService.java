package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.models.Employee;
import java.util.List;

public interface EmployeeService {
    Employee registerEmployee(EmployeeDTO dto);
    Employee getEmployeeById(Long id);
    java.util.List<Employee> getAllEmployees();
    java.util.List<Employee> getEmployeesBySupervisor(Long supervisorId);
    void deleteEmployeeByCode(String code);
    Employee updateEmployee(String code, com.hexaco.hrms.dto.EmployeeUpdateDTO dto);
    Employee updateFingerprintStatus(String code, Boolean fingerprintEnrolled);
    Employee updateProfilePicture(Long id, String profilePicturePath);
    boolean existsByNicNumber(String nicNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEpfNumber(String epfNumber);
    boolean existsByEtfNumber(String etfNumber);
    List<String> getDistinctBranches();
}
