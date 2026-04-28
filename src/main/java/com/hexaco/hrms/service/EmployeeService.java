package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.models.Employee;

public interface EmployeeService {
    Employee registerEmployee(EmployeeDTO dto);
    Employee getEmployeeById(Long id);
    java.util.List<Employee> getAllEmployees();
    void deleteEmployeeByCode(String code);
    Employee updateEmployee(String code, com.hexaco.hrms.dto.EmployeeUpdateDTO dto);
}
